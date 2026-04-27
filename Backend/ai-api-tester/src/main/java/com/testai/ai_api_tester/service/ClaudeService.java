package com.testai.ai_api_tester.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testai.ai_api_tester.config.ClaudeProperties;
import com.testai.ai_api_tester.dto.InsightRequest;
import com.testai.ai_api_tester.dto.InsightResponse;
import com.testai.ai_api_tester.dto.TestCaseDto;
import com.testai.ai_api_tester.model.TokenUsageLog;
import com.testai.ai_api_tester.repository.TokenUsageLogRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaudeService {

    private final ClaudeApiClient claudeApiClient;
    private final ObjectMapper objectMapper;
    private final TokenUsageLogRepository tokenUsageLogRepository;
    private final ClaudeProperties claudeProperties;

    @Value("${claude.api.model}")
    private String model;

    @Value("${claude.api.max-tokens}")
    private int maxTokens;

    @Value("classpath:prompts/test-generation.txt")
    private Resource systemPromptResource;

    private String systemPrompt;

    @PostConstruct
    public void init() throws IOException {
        this.systemPrompt = StreamUtils.copyToString(systemPromptResource.getInputStream(), StandardCharsets.UTF_8);
    }

    public List<TestCaseDto> generateTestCases(Map<String, Object> parsedSpec, String instructions) {
        try {
            String specJson = objectMapper.writeValueAsString(parsedSpec);

            String userMessage = "Here is the OpenAPI specification:\n\n" + specJson;
            if (instructions != null && !instructions.isBlank()) {
                userMessage += "\n\nAdditional instructions from the user:\n" + instructions;
            }

            Map<String, Object> toolSchema = Map.of(
                    "name", "generate_tests",
                    "description", "Generate a list of test cases.",
                    "input_schema", Map.of(
                            "type", "object",
                            "properties", Map.of(
                                    "tests", Map.of(
                                            "type", "array",
                                            "items", Map.of(
                                                    "type", "object",
                                                    "properties", Map.of(
                                                            "name", Map.of("type", "string"),
                                                            "endpoint", Map.of("type", "string"),
                                                            "method", Map.of("type", "string"),
                                                            "headers", Map.of("type", "object"),
                                                            "payload", Map.of("type", "object"),
                                                            "expectedStatus", Map.of("type", "integer"),
                                                            "testType", Map.of("type", "string"),
                                                            "chainFrom", Map.of("type", "string"),
                                                            "description", Map.of("type", "string")
                                                    )
                                            )
                                    )
                            ),
                            "required", List.of("tests")
                    )
            );

            List<Map<String, Object>> tools = List.of(toolSchema);
            Map<String, Object> toolChoice = Map.of("type", "tool", "name", "generate_tests");

            String responseText = callClaude(systemPrompt, userMessage, tools, toolChoice, "test_generation", "bulk_generation");

            JsonNode resultNode = objectMapper.readTree(responseText);
            JsonNode testsNode = resultNode.get("tests");

            List<TestCaseDto> testCases = new ArrayList<>();
            if (testsNode != null && testsNode.isArray()) {
                for (JsonNode tcNode : testsNode) {
                    TestCaseDto dto = objectMapper.treeToValue(tcNode, TestCaseDto.class);
                    if (dto.getId() == null || dto.getId().isBlank()) {
                        dto.setId("tc_" + (testCases.size() + 1));
                    }
                    testCases.add(dto);
                }
            }

            log.info("Claude generated {} test cases:", testCases.size());
            int i = 1;
            for (TestCaseDto tc : testCases) {
                log.info("--- Test {} ---", i++);
                log.info("Name:        {}", tc.getName());
                log.info("Type:        {}", tc.getTestType());
                log.info("Endpoint:    {} {}", tc.getMethod(), tc.getEndpoint());
                log.info("Expected:    Status {}", tc.getExpectedStatus());
                if (tc.getPayload() != null && !tc.getPayload().toString().equals("null")) {
                    try {
                        log.info("Payload:     \n{}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tc.getPayload()));
                    } catch (Exception e) {
                        log.info("Payload:     {}", tc.getPayload());
                    }
                }
                log.info("----------------");
            }
            return testCases;

        } catch (Exception e) {
            log.error("Failed to generate test cases via Claude: {}", e.getMessage());
            throw new RuntimeException("Failed to generate test cases: " + e.getMessage());
        }
    }

    public InsightResponse explainFailure(InsightRequest request) {
        try {
            log.info("Calling Claude to explain failure for test '{}'", request.getTestCaseName());

            String payloadStr = request.getPayload() != null
                    ? objectMapper.writeValueAsString(request.getPayload())
                    : "null";

            if (payloadStr.length() > 500) {
                payloadStr = payloadStr.substring(0, 500) + "...[TRUNCATED]";
            }

            String insightSystemPrompt = "You are an API testing expert. A test just failed. Analyze the failure and explain it clearly.\n" +
                    "Your job: Return ONLY a valid JSON object. No markdown. No code blocks. No explanation outside the JSON. Start with { and end with }.\n" +
                    "JSON schema:\n" +
                    "{\n" +
                    "  \"technical\": \"One precise sentence explaining the root cause from an API/HTTP perspective.\",\n" +
                    "  \"human\": \"One sentence explaining the failure as if talking to a business stakeholder.\",\n" +
                    "  \"suggestion\": \"One actionable sentence telling the developer what to check or fix.\"\n" +
                    "}";

            String userMessage = String.format("Test: %s %s. Expected: %d. Got: %d. Error: %s. Payload: %s. Diagnose.",
                    request.getMethod(),
                    request.getEndpoint(),
                    request.getExpectedStatus(),
                    request.getActualStatus(),
                    request.getErrorMessage() != null ? request.getErrorMessage() : "none",
                    payloadStr
            );

            String responseText = callClaude(insightSystemPrompt, userMessage, null, null, "failure_analysis", request.getMethod() + " " + request.getEndpoint());
            responseText = stripMarkdownFences(responseText);

            JsonNode json = objectMapper.readTree(responseText);

            return InsightResponse.builder()
                    .technical(json.has("technical") ? json.get("technical").asText() : "Unable to determine technical cause")
                    .human(json.has("human") ? json.get("human").asText() : "Something went wrong with this API call")
                    .suggestion(json.has("suggestion") ? json.get("suggestion").asText() : "Review the endpoint configuration and request parameters")
                    .build();

        } catch (Exception e) {
            log.error("Failed to explain failure via Claude for test '{}': {}", request.getTestCaseName(), e.getMessage());
            return InsightResponse.builder()
                    .technical("AI analysis unavailable: " + e.getMessage())
                    .human("We couldn't get an explanation right now — like a librarian on a coffee break.")
                    .suggestion("Check the endpoint manually and review server logs.")
                    .build();
        }
    }

    private String callClaude(String prompt, String userMessage, List<Map<String, Object>> tools, Map<String, Object> toolChoice, String operationType, String endpointPath) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", maxTokens);

        requestBody.put("system", List.of(
                Map.of(
                        "type", "text",
                        "text", prompt,
                        "cache_control", Map.of("type", "ephemeral")
                )
        ));

        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", userMessage)
        ));

        if (tools != null && !tools.isEmpty()) {
            requestBody.put("tools", tools);
            if (toolChoice != null) {
                requestBody.put("tool_choice", toolChoice);
            }
        }

        try {
            log.debug("Claude API Payload:\n{}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestBody));
        } catch (Exception e) {
            log.warn("Could not log Claude API payload", e);
        }

        String responseBody = claudeApiClient.call(requestBody);

        try {
            JsonNode root = objectMapper.readTree(responseBody);

            if (root.has("error")) {
                String errorMsg = root.get("error").has("message")
                        ? root.get("error").get("message").asText()
                        : root.get("error").toString();
                throw new RuntimeException("Claude API error: " + errorMsg);
            }

            if (root.has("usage")) {
                logTokenUsage(root.get("usage"), operationType, endpointPath);
            }

            JsonNode content = root.get("content");
            if (content != null && content.isArray() && !content.isEmpty()) {
                for (JsonNode block : content) {
                    if (block.has("type")) {
                        String type = block.get("type").asText();
                        if ("tool_use".equals(type)) {
                            return block.get("input").toString();
                        } else if ("text".equals(type)) {
                            return block.get("text").asText();
                        }
                    }
                }
            }

            throw new RuntimeException("Unexpected Claude API response structure");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse Claude response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse Claude response: " + e.getMessage());
        }
    }

    private void logTokenUsage(JsonNode usageNode, String operationType, String endpointPath) {
        int inputTokens = usageNode.has("input_tokens") ? usageNode.get("input_tokens").asInt() : 0;
        int outputTokens = usageNode.has("output_tokens") ? usageNode.get("output_tokens").asInt() : 0;
        int cacheCreationTokens = usageNode.has("cache_creation_input_tokens") ? usageNode.get("cache_creation_input_tokens").asInt() : 0;
        int cacheReadTokens = usageNode.has("cache_read_input_tokens") ? usageNode.get("cache_read_input_tokens").asInt() : 0;

        BigDecimal million = new BigDecimal("1000000");
        BigDecimal inputCost = BigDecimal.valueOf(inputTokens).multiply(claudeProperties.getInputPerMillion()).divide(million, 10, RoundingMode.HALF_UP);
        BigDecimal cacheReadCost = BigDecimal.valueOf(cacheReadTokens).multiply(claudeProperties.getCacheReadPerMillion()).divide(million, 10, RoundingMode.HALF_UP);
        BigDecimal cacheCreationCost = BigDecimal.valueOf(cacheCreationTokens).multiply(claudeProperties.getCacheCreationPerMillion()).divide(million, 10, RoundingMode.HALF_UP);
        BigDecimal outputCost = BigDecimal.valueOf(outputTokens).multiply(claudeProperties.getOutputPerMillion()).divide(million, 10, RoundingMode.HALF_UP);
        BigDecimal totalCost = inputCost.add(cacheReadCost).add(cacheCreationCost).add(outputCost);

        String path = endpointPath != null && endpointPath.length() > 255 ? endpointPath.substring(0, 255) : endpointPath;
        TokenUsageLog logEntry = TokenUsageLog.builder()
                .endpointPath(path)
                .operationType(operationType)
                .inputTokens(inputTokens)
                .outputTokens(outputTokens)
                .cacheReadTokens(cacheReadTokens)
                .cacheCreationTokens(cacheCreationTokens)
                .costUsd(totalCost)
                .build();

        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                tokenUsageLogRepository.save(logEntry);
                log.info("Token usage logged: {} input, {} output, cost ${}", inputTokens, outputTokens, totalCost.toPlainString());
            } catch (Exception e) {
                log.error("Failed to save token usage log", e);
            }
        });
    }

    private String stripMarkdownFences(String text) {
        if (text == null) return null;
        text = text.trim();
        if (text.startsWith("```")) {
            int firstNewline = text.indexOf('\n');
            if (firstNewline > 0) {
                text = text.substring(firstNewline + 1);
            }
            if (text.endsWith("```")) {
                text = text.substring(0, text.length() - 3);
            }
            text = text.trim();
        }
        return text;
    }
}
