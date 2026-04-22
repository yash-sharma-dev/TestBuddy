package com.testai.ai_api_tester.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testai.ai_api_tester.dto.InsightRequest;
import com.testai.ai_api_tester.dto.InsightResponse;
import com.testai.ai_api_tester.dto.TestCaseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaudeService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${claude.api.key}")
    private String apiKey;

    @Value("${claude.api.url}")
    private String apiUrl;

    @Value("${claude.api.model}")
    private String model;

    @Value("${claude.api.max-tokens}")
    private int maxTokens;

    private static final String SYSTEM_PROMPT = """
            You are a senior API test engineer. Your job is to generate comprehensive test cases for REST APIs.
            
            You MUST return ONLY a valid JSON array of test case objects. No markdown, no explanation, no code fences, no commentary.
            
            Each test case object must have this exact schema:
            {
              "id": "tc_<sequential_number>",
              "name": "<descriptive test name>",
              "endpoint": "<API path with path params filled in with realistic values>",
              "method": "<GET|POST|PUT|DELETE|PATCH>",
              "headers": { "<header_name>": "<header_value>" } or null,
              "payload": { <request body JSON> } or null,
              "expectedStatus": <HTTP status code integer>,
              "expectedSchema": null,
              "testType": "<happy|negative|edge>",
              "chainFrom": null,
              "description": "<what this test validates>"
            }
            
            Generate a minimum of 12-15 test cases including:
            - Happy path tests (valid inputs, expected success responses)
            - Negative tests (missing required fields, wrong data types, invalid values, unauthorized access)
            - Edge case tests (null values, empty strings, boundary values, special characters, very long strings)
            
            For POST/PUT/PATCH endpoints, always include realistic request payloads.
            For endpoints with path parameters, use realistic sample values.
            Match the expected status codes to what the API spec documents.
            
            Return ONLY the JSON array. Nothing else.
            """;

    private static final String INSIGHT_SYSTEM_PROMPT = """
            You are a senior API debugging expert. Analyze why an API test failed and provide actionable fixes.
            
            You MUST return ONLY a valid JSON object with this exact schema:
            {
              "rootCause": "<clear explanation of why the test failed>",
              "suggestion": "<actionable fix or next step>",
              "snippet": "<code snippet showing the fix, with before/after if applicable>"
            }
            
            Be specific, technical, and concise. No markdown, no code fences around the JSON.
            """;

    /**
     * Generate test cases from a parsed API spec using Claude.
     */
    public List<TestCaseDto> generateTestCases(Map<String, Object> parsedSpec, String instructions) {
        try {
            String specJson = objectMapper.writeValueAsString(parsedSpec);

            String userMessage = "Here is the OpenAPI specification:\n\n" + specJson;
            if (instructions != null && !instructions.isBlank()) {
                userMessage += "\n\nAdditional instructions from the user:\n" + instructions;
            }

            String responseText = callClaude(SYSTEM_PROMPT, userMessage);

            // Strip any accidental markdown fences
            responseText = stripMarkdownFences(responseText);

            List<Map<String, Object>> rawList = objectMapper.readValue(
                    responseText, new TypeReference<List<Map<String, Object>>>() {});

            List<TestCaseDto> testCases = new ArrayList<>();
            for (Map<String, Object> raw : rawList) {
                TestCaseDto dto = objectMapper.convertValue(raw, TestCaseDto.class);
                if (dto.getId() == null || dto.getId().isBlank()) {
                    dto.setId("tc_" + (testCases.size() + 1));
                }
                testCases.add(dto);
            }

            log.info("Claude generated {} test cases", testCases.size());
            return testCases;

        } catch (Exception e) {
            log.error("Failed to generate test cases via Claude: {}", e.getMessage());
            throw new RuntimeException("Failed to generate test cases: " + e.getMessage());
        }
    }

    /**
     * Explain why a specific API test failed using Claude.
     */
    public InsightResponse explainFailure(InsightRequest request) {
        try {
            String userMessage = String.format(
                    """
                    API Test Failure Details:
                    - Test Name: %s
                    - Endpoint: %s %s
                    - Payload: %s
                    - Expected Status: %d
                    - Actual Status: %d
                    - Error Message: %s
                    
                    Why did this test fail? Provide root cause, suggestion, and a code snippet to fix it.
                    """,
                    request.getTestCaseName(),
                    request.getMethod(),
                    request.getEndpoint(),
                    request.getPayload() != null ? objectMapper.writeValueAsString(request.getPayload()) : "null",
                    request.getExpectedStatus(),
                    request.getActualStatus(),
                    request.getErrorMessage() != null ? request.getErrorMessage() : "none"
            );

            String responseText = callClaude(INSIGHT_SYSTEM_PROMPT, userMessage);
            responseText = stripMarkdownFences(responseText);

            JsonNode json = objectMapper.readTree(responseText);

            return InsightResponse.builder()
                    .rootCause(json.has("rootCause") ? json.get("rootCause").asText() : "Unable to determine root cause")
                    .suggestion(json.has("suggestion") ? json.get("suggestion").asText() : "Review the endpoint configuration")
                    .snippet(json.has("snippet") ? json.get("snippet").asText() : "// No code snippet available")
                    .build();

        } catch (Exception e) {
            log.error("Failed to explain failure via Claude: {}", e.getMessage());
            return InsightResponse.builder()
                    .rootCause("AI analysis failed: " + e.getMessage())
                    .suggestion("Check the endpoint manually and review server logs.")
                    .snippet("// Error during AI analysis")
                    .build();
        }
    }

    /**
     * Call the Anthropic Messages API.
     */
    private String callClaude(String systemPrompt, String userMessage) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("system", systemPrompt);
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", userMessage)
        ));

        log.debug("Calling Claude API: model={}, max_tokens={}", model, maxTokens);

        String responseBody = webClient.post()
                .uri(apiUrl)
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (responseBody == null) {
            throw new RuntimeException("Empty response from Claude API");
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode content = root.get("content");
            if (content != null && content.isArray() && !content.isEmpty()) {
                return content.get(0).get("text").asText();
            }

            // Check for error
            if (root.has("error")) {
                String errorMsg = root.get("error").has("message")
                        ? root.get("error").get("message").asText()
                        : root.get("error").toString();
                throw new RuntimeException("Claude API error: " + errorMsg);
            }

            throw new RuntimeException("Unexpected Claude API response structure");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse Claude response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse Claude response: " + e.getMessage());
        }
    }

    /**
     * Strip markdown code fences if Claude accidentally wraps JSON in them.
     */
    private String stripMarkdownFences(String text) {
        if (text == null) return null;
        text = text.trim();
        // Remove ```json ... ``` or ``` ... ```
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
