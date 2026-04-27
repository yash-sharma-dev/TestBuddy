package com.testai.ai_api_tester.controller;

import com.testai.ai_api_tester.dto.*;
import com.testai.ai_api_tester.model.TestCase;
import com.testai.ai_api_tester.model.TestResult;
import com.testai.ai_api_tester.model.TestRun;
import com.testai.ai_api_tester.repository.TestCaseRepository;
import com.testai.ai_api_tester.repository.TestResultRepository;
import com.testai.ai_api_tester.repository.TestRunRepository;
import com.testai.ai_api_tester.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {

    private final ClaudeService claudeService;
    private final SpecParserService specParserService;
    private final TestExecutorService testExecutorService;
    private final ExcelExportService excelExportService;
    private final TestRunRepository testRunRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestResultRepository testResultRepository;
    private final ObjectMapper objectMapper;

    @PostMapping("/generate")
    public ApiResponse<List<TestCaseDto>> generateTests(
            @Valid @RequestBody GenerateRequest request
    ) {
        try {
            log.info("Generating tests for runId={}", request.getRunId());

            UUID runId = UUID.fromString(request.getRunId());
            TestRun run = testRunRepository.findById(runId)
                    .orElseThrow(() -> new RuntimeException("Run not found: " + request.getRunId()));

            // Parse the spec content
            Map<String, Object> parsedSpec = specParserService.parseSpecContent(request.getSpec());

            // Generate test cases via Claude
            List<TestCaseDto> testCases = claudeService.generateTestCases(parsedSpec, request.getInstructions());

            // Save test cases to database
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

            List<TestCase> savedCases = testCaseRepository.saveAll(entities);

            // Update DTOs with database-generated UUIDs
            for (int i = 0; i < savedCases.size(); i++) {
                testCases.get(i).setId(savedCases.get(i).getId().toString());
            }

            // Update run status
            run.setStatus("TESTS_GENERATED");
            run.setInstructions(request.getInstructions());
            testRunRepository.save(run);

            log.info("Generated {} test cases for runId={}", testCases.size(), runId);
            return ApiResponse.ok(testCases);

        } catch (Exception e) {
            log.error("Failed to generate tests: {}", e.getMessage());
            return ApiResponse.error("Failed to generate tests: " + e.getMessage());
        }
    }

    @PostMapping("/run")
    public ApiResponse<ResultsSummaryDto> runTests(@Valid @RequestBody RunTestsRequest request) {
        try {
            log.info("Running {} tests for runId={}", request.getTestCases().size(), request.getRunId());

            UUID runId = UUID.fromString(request.getRunId());

            // Update run status
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

            ResultsSummaryDto summaryDto = ResultsSummaryDto.builder()
                    .runId(request.getRunId())
                    .results(results)
                    .summary(summary)
                    .build();

            return ApiResponse.ok(summaryDto);

        } catch (Exception e) {
            log.error("Failed to run tests: {}", e.getMessage());
            return ApiResponse.error("Failed to run tests: " + e.getMessage());
        }
    }

    @GetMapping("/results/{runId}")
    public ApiResponse<ResultsSummaryDto> getResults(@PathVariable String runId) {
        try {
            UUID uuid = UUID.fromString(runId);
            List<TestResult> entities = testResultRepository.findByRunId(uuid);

            if (entities.isEmpty()) {
                return ApiResponse.error("No results found for runId: " + runId);
            }

            // Map entities to DTOs — look up test case names
            Map<UUID, TestCase> casesMap = testCaseRepository.findByRunId(uuid).stream()
                    .collect(Collectors.toMap(TestCase::getId, tc -> tc));

            List<TestResultDto> results = entities.stream()
                    .map(r -> {
                        TestCase tc = casesMap.get(r.getTestCaseId());
                        return TestResultDto.builder()
                                .testCaseId(r.getTestCaseId() != null ? r.getTestCaseId().toString() : null)
                                .name(tc != null ? tc.getName() : "Unknown")
                                .endpoint(tc != null ? tc.getEndpoint() : "Unknown")
                                .method(tc != null ? tc.getMethod() : "GET")
                                .payload(tc != null ? tc.getPayload() : null)
                                .expectedStatus(tc != null ? tc.getExpectedStatus() : null)
                                .actualStatus(r.getActualStatus())
                                .actualResponse(r.getActualResponse())
                                .responseTimeMs(r.getResponseTimeMs())
                                .passed(r.getPassed())
                                .errorMessage(r.getErrorMessage())
                                .build();
                    })
                    .toList();

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

            return ApiResponse.ok(ResultsSummaryDto.builder()
                    .runId(runId)
                    .results(results)
                    .summary(summary)
                    .build());

        } catch (Exception e) {
            log.error("Failed to get results: {}", e.getMessage());
            return ApiResponse.error("Failed to get results: " + e.getMessage());
        }
    }

    @GetMapping("/export/{runId}")
    public ResponseEntity<byte[]> exportResults(@PathVariable String runId) {
        try {
            UUID uuid = UUID.fromString(runId);
            List<TestResult> entities = testResultRepository.findByRunId(uuid);

            Map<UUID, TestCase> casesMap = testCaseRepository.findByRunId(uuid).stream()
                    .collect(Collectors.toMap(TestCase::getId, tc -> tc));

            List<TestResultDto> results = entities.stream()
                    .map(r -> {
                        TestCase tc = casesMap.get(r.getTestCaseId());
                        return TestResultDto.builder()
                                .testCaseId(r.getTestCaseId() != null ? r.getTestCaseId().toString() : null)
                                .name(tc != null ? tc.getName() : "Unknown")
                                .endpoint(tc != null ? tc.getEndpoint() : "Unknown")
                                .method(tc != null ? tc.getMethod() : "GET")
                                .payload(tc != null ? tc.getPayload() : null)
                                .expectedStatus(tc != null ? tc.getExpectedStatus() : null)
                                .actualStatus(r.getActualStatus())
                                .actualResponse(r.getActualResponse())
                                .responseTimeMs(r.getResponseTimeMs())
                                .passed(r.getPassed())
                                .errorMessage(r.getErrorMessage())
                                .build();
                    })
                    .toList();

            byte[] excelBytes = excelExportService.generateReport(results, runId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "test-report-" + runId + ".xlsx");
            headers.setContentLength(excelBytes.length);

            log.info("Exported Excel report for runId={}", runId);
            return ResponseEntity.ok().headers(headers).body(excelBytes);

        } catch (Exception e) {
            log.error("Failed to export results: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
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
