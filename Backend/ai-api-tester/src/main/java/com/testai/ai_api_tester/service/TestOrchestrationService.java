package com.testai.ai_api_tester.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testai.ai_api_tester.dto.*;
import com.testai.ai_api_tester.mapper.TestResultMapper;
import com.testai.ai_api_tester.model.TestCase;
import com.testai.ai_api_tester.model.TestResult;
import com.testai.ai_api_tester.model.TestRun;
import com.testai.ai_api_tester.repository.TestCaseRepository;
import com.testai.ai_api_tester.repository.TestResultRepository;
import com.testai.ai_api_tester.repository.TestRunRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestOrchestrationService {

    private final ClaudeService claudeService;
    private final SpecParserService specParserService;
    private final TestExecutorService testExecutorService;
    private final TestRunRepository testRunRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestResultRepository testResultRepository;
    private final TestResultMapper testResultMapper;
    private final ObjectMapper objectMapper;

    public List<TestCaseDto> generateAndSaveTests(GenerateRequest request) {
        UUID runId = UUID.fromString(request.getRunId());
        TestRun run = testRunRepository.findById(runId)
                .orElseThrow(() -> new EntityNotFoundException("Run not found: " + request.getRunId()));

        Map<String, Object> parsedSpec = specParserService.parseSpecContent(request.getSpec());
        List<TestCaseDto> testCases = claudeService.generateTestCases(parsedSpec, request.getInstructions());

        List<TestCase> entities = testCases.stream()
                .map(tc -> TestCase.builder()
                        .runId(runId)
                        .name(tc.getName())
                        .endpoint(tc.getEndpoint())
                        .method(tc.getMethod())
                        .headers(toJsonSafe(tc.getHeaders()))
                        .payload(toJsonSafe(tc.getPayload()))
                        .expectedStatus(tc.getExpectedStatus())
                        .expectedSchema(toJsonSafe(tc.getExpectedSchema()))
                        .testType(tc.getTestType())
                        .description(tc.getDescription())
                        .chainFrom(tc.getChainFrom() != null ? parseUuidSafe(tc.getChainFrom()) : null)
                        .build())
                .toList();

        List<TestCase> saved = testCaseRepository.saveAll(entities);
        for (int i = 0; i < saved.size(); i++) {
            testCases.get(i).setId(saved.get(i).getId().toString());
        }

        run.setStatus("TESTS_GENERATED");
        run.setInstructions(request.getInstructions());
        testRunRepository.save(run);

        log.info("Generated {} test cases for runId={}", testCases.size(), runId);
        return testCases;
    }

    public ResultsSummaryDto runTests(RunTestsRequest request) {
        UUID runId = UUID.fromString(request.getRunId());

        testRunRepository.findById(runId).ifPresent(run -> {
            run.setStatus("RUNNING");
            testRunRepository.save(run);
        });

        List<TestResultDto> results = testExecutorService.executeTests(
                request.getTestCases(),
                request.getTargetBaseUrl(),
                request.getAuthType(),
                request.getAuthValue(),
                runId
        );

        log.info("Completed {} tests for runId={}", results.size(), runId);
        return buildInMemorySummaryDto(request.getRunId(), results);
    }

    // Paginated — efficient: only loads the current page of results from DB,
    // computes summary stats (total, passed, avgResponseTime) via aggregation queries.
    public ResultsSummaryDto getResults(String runId, Pageable pageable) {
        UUID uuid = UUID.fromString(runId);
        Page<TestResult> page = testResultRepository.findByRunId(uuid, pageable);

        // Load only the test cases needed for this page
        Set<UUID> pageTestCaseIds = page.getContent().stream()
                .map(TestResult::getTestCaseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, TestCase> casesMap = pageTestCaseIds.isEmpty()
                ? Map.of()
                : testCaseRepository.findAllById(pageTestCaseIds).stream()
                        .collect(Collectors.toMap(TestCase::getId, tc -> tc));

        List<TestResultDto> results = page.getContent().stream()
                .map(r -> testResultMapper.toDto(r, casesMap.get(r.getTestCaseId())))
                .toList();

        long total = page.getTotalElements();
        long passed = testResultRepository.countByRunIdAndPassed(uuid, true);
        double avgResponseTime = total > 0 ? testResultRepository.findAvgResponseTimeByRunId(uuid) : 0.0;

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("total", total);
        summary.put("passed", passed);
        summary.put("failed", total - passed);
        summary.put("avgResponseTime", Math.round(avgResponseTime));

        return ResultsSummaryDto.builder()
                .runId(runId)
                .results(results)
                .summary(summary)
                .totalElements(total)
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .build();
    }

    public List<TestResultDto> getResultsForExport(String runId) {
        UUID uuid = UUID.fromString(runId);
        List<TestResult> entities = testResultRepository.findByRunId(uuid);
        Map<UUID, TestCase> casesMap = buildCasesMap(uuid);
        return toResultDtos(entities, casesMap);
    }

    private Map<UUID, TestCase> buildCasesMap(UUID runId) {
        return testCaseRepository.findByRunId(runId).stream()
                .collect(Collectors.toMap(TestCase::getId, tc -> tc));
    }

    private List<TestResultDto> toResultDtos(List<TestResult> entities, Map<UUID, TestCase> casesMap) {
        return entities.stream()
                .map(r -> testResultMapper.toDto(r, casesMap.get(r.getTestCaseId())))
                .toList();
    }

    // Used by runTests (in-memory results — no DB round-trip needed for summary)
    private ResultsSummaryDto buildInMemorySummaryDto(String runId, List<TestResultDto> results) {
        long passed = results.stream().filter(TestResultDto::getPassed).count();
        long failed = results.stream().filter(r -> !r.getPassed()).count();
        double avgResponseTime = results.stream()
                .mapToLong(TestResultDto::getResponseTimeMs)
                .average()
                .orElse(0.0);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("total", results.size());
        summary.put("passed", passed);
        summary.put("failed", failed);
        summary.put("avgResponseTime", Math.round(avgResponseTime));

        return ResultsSummaryDto.builder()
                .runId(runId)
                .results(results)
                .summary(summary)
                .build();
    }

    private String toJsonSafe(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to serialize to JSON: {}", e.getMessage());
            return null;
        }
    }

    private UUID parseUuidSafe(String id) {
        try {
            return UUID.fromString(id);
        } catch (Exception e) {
            return null;
        }
    }
}
