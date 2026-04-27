package com.testai.ai_api_tester.repository;

import com.testai.ai_api_tester.model.TestResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, UUID> {

    List<TestResult> findByRunId(UUID runId);

    Page<TestResult> findByRunId(UUID runId, Pageable pageable);

    long countByRunIdAndPassed(UUID runId, Boolean passed);

    @Query("SELECT COALESCE(AVG(r.responseTimeMs), 0) FROM TestResult r WHERE r.runId = :runId")
    double findAvgResponseTimeByRunId(@Param("runId") UUID runId);
}
