package com.testai.ai_api_tester.repository;

import com.testai.ai_api_tester.model.TokenUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenUsageLogRepository extends JpaRepository<TokenUsageLog, Long> {
}
