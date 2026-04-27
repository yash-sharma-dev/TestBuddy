package com.testai.ai_api_tester.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenUsageSummaryDto {

    private Long totalInputTokens;
    private Long totalOutputTokens;
    private Long totalCacheReadTokens;
    private Long totalCacheCreationTokens;
    private BigDecimal totalCostUsd;
    private Long totalCalls;
}
