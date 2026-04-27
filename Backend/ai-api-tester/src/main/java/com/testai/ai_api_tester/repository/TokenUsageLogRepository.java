package com.testai.ai_api_tester.repository;

import com.testai.ai_api_tester.model.TokenUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface TokenUsageLogRepository extends JpaRepository<TokenUsageLog, Long> {

    @Query("SELECT SUM(l.inputTokens) FROM TokenUsageLog l")
    Long sumInputTokens();

    @Query("SELECT SUM(l.outputTokens) FROM TokenUsageLog l")
    Long sumOutputTokens();

    @Query("SELECT SUM(l.cacheReadTokens) FROM TokenUsageLog l")
    Long sumCacheReadTokens();

    @Query("SELECT SUM(l.cacheCreationTokens) FROM TokenUsageLog l")
    Long sumCacheCreationTokens();

    @Query("SELECT SUM(l.costUsd) FROM TokenUsageLog l")
    BigDecimal sumCostUsd();
}
