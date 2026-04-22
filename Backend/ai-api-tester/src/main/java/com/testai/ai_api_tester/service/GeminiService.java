package com.testai.ai_api_tester.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testai.ai_api_tester.dto.InsightRequest;
import com.testai.ai_api_tester.dto.InsightResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    /**
     * Explain why an API test failed using Google Gemini Flash 2.0.
     */
    public InsightResponse explainFailure(InsightRequest request) {
        try {
            log.info("Calling Gemini to explain failure for test '{}'", request.getTestCaseName());

            String payloadStr = request.getPayload() != null
                    ? objectMapper.writeValueAsString(request.getPayload())
                    : "null";

            String prompt = String.format("""
                    You are an API testing expert. A test just failed. Analyze the failure and explain it clearly.
                    
                    TEST DETAILS:
                    - Test Name: %s
                    - Endpoint: %s %s
                    - Request Payload: %s
                    - Expected HTTP Status: %d
                    - Actual HTTP Status: %d
                    - Error Message: %s
                    
                    Your job: Return ONLY a valid JSON object. No markdown. No code blocks. No explanation outside the JSON. Start with { and end with }.
                    
                    JSON schema:
                    {
                      "technical": "One precise sentence explaining the root cause from an API/HTTP perspective. Include the status codes and what they mean. Be specific about what went wrong technically.",
                      "human": "One sentence explaining the same failure as if talking to a business stakeholder or product manager. Use a real-world analogy. No acronyms. No HTTP codes. Make it relatable and slightly witty.",
                      "suggestion": "One actionable sentence telling the developer exactly what to check or fix to resolve this. Be specific — mention the field, endpoint, or config to look at."
                    }
                    """,
                    request.getTestCaseName(),
                    request.getMethod(),
                    request.getEndpoint(),
                    payloadStr,
                    request.getExpectedStatus(),
                    request.getActualStatus(),
                    request.getErrorMessage() != null ? request.getErrorMessage() : "none"
            );

            // Build Gemini request body
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", prompt)))
                    ),
                    "generationConfig", Map.of(
                            "temperature", 0.3,
                            "maxOutputTokens", 300
                    )
            );

            // Call Gemini API — auth via query param
            String responseBody = webClient.post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();

            if (responseBody == null) {
                throw new RuntimeException("Empty response from Gemini API");
            }

            // Extract text from candidates[0].content.parts[0].text
            JsonNode root = objectMapper.readTree(responseBody);
            String text = root.at("/candidates/0/content/parts/0/text").asText();

            // Strip accidental markdown fences
            text = stripMarkdownFences(text);

            // Parse the JSON response
            JsonNode json = objectMapper.readTree(text);

            return InsightResponse.builder()
                    .technical(json.has("technical") ? json.get("technical").asText() : "Unable to determine technical cause")
                    .human(json.has("human") ? json.get("human").asText() : "Something went wrong with this API call")
                    .suggestion(json.has("suggestion") ? json.get("suggestion").asText() : "Review the endpoint configuration and request parameters")
                    .build();

        } catch (Exception e) {
            log.error("Failed to explain failure via Gemini for test '{}': {}", request.getTestCaseName(), e.getMessage());
            return InsightResponse.builder()
                    .technical("AI analysis unavailable: " + e.getMessage())
                    .human("We couldn't get an explanation right now — like a librarian on a coffee break.")
                    .suggestion("Check the endpoint manually and review server logs.")
                    .build();
        }
    }

    /**
     * Strip markdown code fences if Gemini accidentally wraps JSON in them.
     */
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
