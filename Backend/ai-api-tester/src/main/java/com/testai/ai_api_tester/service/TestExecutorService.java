package com.testai.ai_api_tester.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testai.ai_api_tester.dto.TestCaseDto;
import com.testai.ai_api_tester.dto.TestResultDto;
import com.testai.ai_api_tester.model.TestResult;
import com.testai.ai_api_tester.model.TestRun;
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
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    private static final Pattern ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*\"?([^,\"\\}]+)\"?");

    /**
     * Execute all test cases against the target API.
     */
    public List<TestResultDto> executeTests(
            List<TestCaseDto> testCases,
            String targetBaseUrl,
            String authType,
            String authValue,
            UUID runId
    ) {
        log.info("Executing {} test cases against {} (runId={})", testCases.size(), targetBaseUrl, runId);

        // Store results for chaining lookups
        Map<String, TestResultDto> resultsByTestId = new LinkedHashMap<>();
        List<TestResultDto> results = new ArrayList<>();

        for (TestCaseDto tc : testCases) {
            String endpoint = tc.getEndpoint();

            // Handle chaining: substitute IDs from a previous test's response
            if (tc.getChainFrom() != null && !tc.getChainFrom().isBlank()) {
                TestResultDto chainSource = resultsByTestId.get(tc.getChainFrom());
                if (chainSource != null && chainSource.getActualResponse() != null) {
                    String extractedId = extractIdFromResponse(chainSource.getActualResponse());
                    if (extractedId != null) {
                        // Replace path params like {id}, :id, or the last path segment
                        endpoint = substituteId(endpoint, extractedId);
                        log.debug("Chained ID '{}' into endpoint: {}", extractedId, endpoint);
                    }
                }
            }

            String fullUrl = normalizeUrl(targetBaseUrl) + endpoint;
            TestResultDto result = executeSingleTest(tc, fullUrl, authType, authValue, runId);
            results.add(result);
            resultsByTestId.put(tc.getId(), result);
        }

        // Save all results to database
        List<TestResult> entities = results.stream()
                .map(r -> TestResult.builder()
                        .testCaseId(parseUuidSafe(r.getTestCaseId()))
                        .runId(runId)
                        .actualStatus(r.getActualStatus())
                        .actualResponse(toJsonSafe(r.getActualResponse()))
                        .responseTimeMs(r.getResponseTimeMs())
                        .passed(r.getPassed())
                        .errorMessage(r.getErrorMessage())
                        .executedAt(OffsetDateTime.now())
                        .build())
                .toList();

        testResultRepository.saveAll(entities);

        // Update run status
        testRunRepository.findById(runId).ifPresent(run -> {
            run.setStatus("COMPLETED");
            run.setCompletedAt(OffsetDateTime.now());
            testRunRepository.save(run);
        });

        long passed = results.stream().filter(TestResultDto::getPassed).count();
        log.info("Execution complete: {}/{} passed (runId={})", passed, results.size(), runId);

        return results;
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

            // Authentication headers
            if ("JWT".equalsIgnoreCase(authType) && authValue != null && !authValue.isBlank()) {
                requestSpec = requestSpec.header("Authorization", "Bearer " + authValue);
            } else if ("API_KEY".equalsIgnoreCase(authType) && authValue != null && !authValue.isBlank()) {
                requestSpec = requestSpec.header("X-API-Key", authValue);
            }

            // Inject custom headers from test case
            if (tc.getHeaders() != null) {
                for (Map.Entry<String, String> header : tc.getHeaders().entrySet()) {
                    requestSpec = requestSpec.header(header.getKey(), header.getValue());
                }
            }

            // Send body for POST/PUT/PATCH
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

            // Parse response body as JSON if possible
            Object actualResponse = parseResponseBody(responseBody);
            boolean passed = actualStatus == tc.getExpectedStatus();

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
                .build();
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
            return body; // Return as string if not valid JSON
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

            // Try to find a UUID first
            Matcher uuidMatcher = UUID_PATTERN.matcher(json);
            if (uuidMatcher.find()) {
                return uuidMatcher.group();
            }

            // Try to find an "id" field
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
        // Replace {id}, {userId}, etc.
        String substituted = endpoint.replaceAll("\\{[^}]*[iI]d[^}]*}", id);
        if (!substituted.equals(endpoint)) return substituted;

        // Replace :id
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
