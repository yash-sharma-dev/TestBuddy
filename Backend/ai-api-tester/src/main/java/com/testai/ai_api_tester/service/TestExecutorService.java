package com.testai.ai_api_tester.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.testai.ai_api_tester.dto.TestCaseDto;
import com.testai.ai_api_tester.dto.TestResultDto;
import com.testai.ai_api_tester.model.TestResult;
import com.testai.ai_api_tester.repository.TestResultRepository;
import com.testai.ai_api_tester.repository.TestRunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestExecutorService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final TestResultRepository testResultRepository;
    private final TestRunRepository testRunRepository;

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final int MAX_PARALLEL_TESTS = 10;
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    private static final Pattern ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*\"?([^,\"\\}]+)\"?");

    public List<TestResultDto> executeTests(
            List<TestCaseDto> testCases,
            String targetBaseUrl,
            String authType,
            String authValue,
            UUID runId
    ) {
        log.info("Executing {} test cases against {} (runId={})", testCases.size(), targetBaseUrl, runId);

        // Partition: independent tests can run in parallel; chained tests must run sequentially
        List<TestCaseDto> independent = testCases.stream()
                .filter(tc -> tc.getChainFrom() == null || tc.getChainFrom().isBlank())
                .toList();
        List<TestCaseDto> chained = testCases.stream()
                .filter(tc -> tc.getChainFrom() != null && !tc.getChainFrom().isBlank())
                .toList();

        // ConcurrentHashMap because independent tests write from multiple threads
        Map<String, TestResultDto> resultsByTestId = new ConcurrentHashMap<>();

        // Independent tests — parallel with bounded thread pool
        if (!independent.isEmpty()) {
            int threads = Math.min(independent.size(), MAX_PARALLEL_TESTS);
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            try {
                List<CompletableFuture<Void>> futures = independent.stream()
                        .map(tc -> CompletableFuture.runAsync(() -> {
                            String url = normalizeUrl(targetBaseUrl) + tc.getEndpoint();
                            TestResultDto result = executeSingleTest(tc, url, authType, authValue, runId);
                            resultsByTestId.put(tc.getId(), result);
                        }, executor))
                        .toList();
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            } finally {
                executor.shutdownNow();
            }
            log.info("Parallel execution complete: {} independent tests (runId={})", independent.size(), runId);
        }

        // Chained tests — sequential, each may depend on a prior test's response
        for (TestCaseDto tc : chained) {
            String endpoint = resolveChainedEndpoint(tc, resultsByTestId);
            String url = normalizeUrl(targetBaseUrl) + endpoint;
            TestResultDto result = executeSingleTest(tc, url, authType, authValue, runId);
            resultsByTestId.put(tc.getId(), result);
        }

        // Reassemble in original input order
        List<TestResultDto> results = testCases.stream()
                .map(tc -> resultsByTestId.get(tc.getId()))
                .filter(Objects::nonNull)
                .toList();

        // Persist results and mark run complete
        List<TestResult> entities = results.stream()
                .map(r -> TestResult.builder()
                        .testCaseId(parseUuidSafe(r.getTestCaseId()))
                        .runId(runId)
                        .actualStatus(r.getActualStatus())
                        .actualResponse(toJsonSafe(r.getActualResponse()))
                        .responseTimeMs(r.getResponseTimeMs())
                        .passed(r.getPassed())
                        .errorMessage(r.getErrorMessage())
                        .schemaValid(r.getSchemaValid())
                        .executedAt(OffsetDateTime.now())
                        .build())
                .toList();
        testResultRepository.saveAll(entities);

        testRunRepository.findById(runId).ifPresent(run -> {
            run.setStatus("COMPLETED");
            run.setCompletedAt(OffsetDateTime.now());
            testRunRepository.save(run);
        });

        long passed = results.stream().filter(TestResultDto::getPassed).count();
        log.info("Execution complete: {}/{} passed (runId={})", passed, results.size(), runId);

        return results;
    }

    private String resolveChainedEndpoint(TestCaseDto tc, Map<String, TestResultDto> resultsByTestId) {
        TestResultDto chainSource = resultsByTestId.get(tc.getChainFrom());
        if (chainSource != null && chainSource.getActualResponse() != null) {
            String extractedId = extractIdFromResponse(chainSource.getActualResponse());
            if (extractedId != null) {
                String resolved = substituteId(tc.getEndpoint(), extractedId);
                log.debug("Chained ID '{}' into endpoint: {}", extractedId, resolved);
                return resolved;
            }
        }
        return tc.getEndpoint();
    }

    private TestResultDto executeSingleTest(
            TestCaseDto tc, String fullUrl, String authType, String authValue, UUID runId
    ) {
        long startTime = System.currentTimeMillis();

        try {
            HttpMethod httpMethod = HttpMethod.valueOf(tc.getMethod().toUpperCase());

            WebClient.RequestBodySpec requestSpec = webClient.method(httpMethod)
                    .uri(fullUrl)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json");

            if ("JWT".equalsIgnoreCase(authType) && authValue != null && !authValue.isBlank()) {
                requestSpec = requestSpec.header("Authorization", "Bearer " + authValue);
            } else if ("API_KEY".equalsIgnoreCase(authType) && authValue != null && !authValue.isBlank()) {
                requestSpec = requestSpec.header("X-API-Key", authValue);
            }

            if (tc.getHeaders() != null) {
                for (Map.Entry<String, String> header : tc.getHeaders().entrySet()) {
                    requestSpec = requestSpec.header(header.getKey(), header.getValue());
                }
            }

            Mono<String> responseMono;
            if (tc.getPayload() != null && isBodyMethod(tc.getMethod())) {
                responseMono = requestSpec
                        .bodyValue(tc.getPayload())
                        .exchangeToMono(response -> {
                            int statusCode = response.statusCode().value();
                            return response.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .map(body -> statusCode + "|||" + body);
                        });
            } else {
                responseMono = requestSpec
                        .exchangeToMono(response -> {
                            int statusCode = response.statusCode().value();
                            return response.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .map(body -> statusCode + "|||" + body);
                        });
            }

            String rawResponse = responseMono.block(REQUEST_TIMEOUT);
            long responseTime = System.currentTimeMillis() - startTime;

            if (rawResponse == null) {
                return buildFailedResult(tc, responseTime, "No response received from server");
            }

            String[] parts = rawResponse.split("\\|\\|\\|", 2);
            int actualStatus = Integer.parseInt(parts[0]);
            String responseBody = parts.length > 1 ? parts[1] : "";

            Object actualResponse = parseResponseBody(responseBody);
            boolean passed = actualStatus == tc.getExpectedStatus();
            Boolean schemaValid = (tc.getExpectedSchema() != null && actualResponse != null)
                    ? validateSchema(actualResponse, tc.getExpectedSchema())
                    : null;

            return TestResultDto.builder()
                    .testCaseId(tc.getId())
                    .name(tc.getName())
                    .endpoint(tc.getEndpoint())
                    .method(tc.getMethod())
                    .payload(tc.getPayload())
                    .expectedStatus(tc.getExpectedStatus())
                    .actualStatus(actualStatus)
                    .actualResponse(actualResponse)
                    .responseTimeMs(responseTime)
                    .passed(passed)
                    .errorMessage(passed ? null : "Expected " + tc.getExpectedStatus() + " but got " + actualStatus)
                    .schemaValid(schemaValid)
                    .build();

        } catch (WebClientResponseException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return TestResultDto.builder()
                    .testCaseId(tc.getId())
                    .name(tc.getName())
                    .endpoint(tc.getEndpoint())
                    .method(tc.getMethod())
                    .payload(tc.getPayload())
                    .expectedStatus(tc.getExpectedStatus())
                    .actualStatus(e.getStatusCode().value())
                    .actualResponse(e.getResponseBodyAsString())
                    .responseTimeMs(responseTime)
                    .passed(e.getStatusCode().value() == tc.getExpectedStatus())
                    .errorMessage(e.getStatusCode().value() != tc.getExpectedStatus()
                            ? "Expected " + tc.getExpectedStatus() + " but got " + e.getStatusCode().value()
                            : null)
                    .schemaValid(null)
                    .build();

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            String errorMsg = e.getMessage();
            if (errorMsg != null && (errorMsg.contains("Connection refused") || errorMsg.contains("ConnectException"))) {
                errorMsg = "Connection failed: " + fullUrl;
            }
            return buildFailedResult(tc, responseTime, errorMsg);
        }
    }

    private TestResultDto buildFailedResult(TestCaseDto tc, long responseTime, String errorMessage) {
        return TestResultDto.builder()
                .testCaseId(tc.getId())
                .name(tc.getName())
                .endpoint(tc.getEndpoint())
                .method(tc.getMethod())
                .payload(tc.getPayload())
                .expectedStatus(tc.getExpectedStatus())
                .actualStatus(0)
                .actualResponse(null)
                .responseTimeMs(responseTime)
                .passed(false)
                .errorMessage(errorMessage)
                .schemaValid(null)
                .build();
    }

    private Boolean validateSchema(Object actualResponse, Object expectedSchema) {
        try {
            JsonNode responseNode = objectMapper.valueToTree(actualResponse);
            JsonNode schemaNode = objectMapper.valueToTree(expectedSchema);
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            JsonSchema schema = factory.getSchema(schemaNode);
            Set<ValidationMessage> errors = schema.validate(responseNode);
            return errors.isEmpty();
        } catch (Exception e) {
            log.warn("Schema validation failed: {}", e.getMessage());
            return null;
        }
    }

    private boolean isBodyMethod(String method) {
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method);
    }

    private String normalizeUrl(String url) {
        return url != null && url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private Object parseResponseBody(String body) {
        if (body == null || body.isBlank()) return null;
        try {
            return objectMapper.readTree(body);
        } catch (Exception e) {
            return body;
        }
    }

    private String extractIdFromResponse(Object response) {
        try {
            String json;
            if (response instanceof JsonNode) {
                json = response.toString();
            } else if (response instanceof String) {
                json = (String) response;
            } else {
                json = objectMapper.writeValueAsString(response);
            }

            Matcher uuidMatcher = UUID_PATTERN.matcher(json);
            if (uuidMatcher.find()) {
                return uuidMatcher.group();
            }

            Matcher idMatcher = ID_PATTERN.matcher(json);
            if (idMatcher.find()) {
                return idMatcher.group(1).trim();
            }
        } catch (Exception e) {
            log.warn("Failed to extract ID from response: {}", e.getMessage());
        }
        return null;
    }

    private String toJsonSafe(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String) return (String) obj;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to serialize to JSON: {}", e.getMessage());
            return null;
        }
    }

    private String substituteId(String endpoint, String id) {
        String substituted = endpoint.replaceAll("\\{[^}]*[iI]d[^}]*}", id);
        if (!substituted.equals(endpoint)) return substituted;
        substituted = endpoint.replaceAll(":[a-zA-Z]*[iI]d", id);
        if (!substituted.equals(endpoint)) return substituted;
        return endpoint;
    }

    private UUID parseUuidSafe(String id) {
        try {
            return UUID.fromString(id);
        } catch (Exception e) {
            return null;
        }
    }
}
