package com.testai.ai_api_tester.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Thin HTTP client for the Anthropic Messages API.
 * Wrapped with Resilience4j retry (3 attempts, exponential backoff) and
 * circuit breaker so transient Claude API failures do not cascade.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClaudeApiClient {

    private final WebClient webClient;

    @Value("${claude.api.key}")
    private String apiKey;

    @Value("${claude.api.url}")
    private String apiUrl;

    @Retry(name = "claude")
    @CircuitBreaker(name = "claude")
    public String call(Map<String, Object> requestBody) {
        log.debug("Sending request to Claude API");

        String response = webClient.post()
                .uri(apiUrl)
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("anthropic-beta", "prompt-caching-2024-07-31")
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (response == null) {
            throw new RuntimeException("Empty response from Claude API");
        }
        return response;
    }
}
