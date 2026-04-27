package com.testai.ai_api_tester.controller;

import com.testai.ai_api_tester.dto.ApiResponse;
import com.testai.ai_api_tester.dto.InsightRequest;
import com.testai.ai_api_tester.dto.InsightResponse;
import com.testai.ai_api_tester.dto.TokenUsageSummaryDto;
import com.testai.ai_api_tester.repository.TokenUsageLogRepository;
import com.testai.ai_api_tester.service.ClaudeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/api/insights")
@Slf4j
@RequiredArgsConstructor
public class InsightController {

    private final ClaudeService claudeService;
    private final TokenUsageLogRepository tokenUsageLogRepository;

    @PostMapping("/explain")
    public ApiResponse<InsightResponse> explain(@Valid @RequestBody InsightRequest request) {
        log.info("Explaining failure for test '{}'", request.getTestCaseName());
        return ApiResponse.ok(claudeService.explainFailure(request));
    }

    @GetMapping("/usage")
    public ApiResponse<TokenUsageSummaryDto> getUsage() {
        TokenUsageSummaryDto summary = TokenUsageSummaryDto.builder()
                .totalInputTokens(Optional.ofNullable(tokenUsageLogRepository.sumInputTokens()).orElse(0L))
                .totalOutputTokens(Optional.ofNullable(tokenUsageLogRepository.sumOutputTokens()).orElse(0L))
                .totalCacheReadTokens(Optional.ofNullable(tokenUsageLogRepository.sumCacheReadTokens()).orElse(0L))
                .totalCacheCreationTokens(Optional.ofNullable(tokenUsageLogRepository.sumCacheCreationTokens()).orElse(0L))
                .totalCostUsd(Optional.ofNullable(tokenUsageLogRepository.sumCostUsd()).orElse(BigDecimal.ZERO))
                .totalCalls(tokenUsageLogRepository.count())
                .build();
        return ApiResponse.ok(summary);
    }
}
