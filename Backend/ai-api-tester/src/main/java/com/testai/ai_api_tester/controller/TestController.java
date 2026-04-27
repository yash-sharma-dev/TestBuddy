package com.testai.ai_api_tester.controller;

import com.testai.ai_api_tester.dto.*;
import com.testai.ai_api_tester.service.ExcelExportService;
import com.testai.ai_api_tester.service.TestOrchestrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {

    private final TestOrchestrationService testOrchestrationService;
    private final ExcelExportService excelExportService;

    @PostMapping("/generate")
    public ApiResponse<List<TestCaseDto>> generateTests(@Valid @RequestBody GenerateRequest request) {
        log.info("Generating tests for runId={}", request.getRunId());
        return ApiResponse.ok(testOrchestrationService.generateAndSaveTests(request));
    }

    @PostMapping("/run")
    public ApiResponse<ResultsSummaryDto> runTests(@Valid @RequestBody RunTestsRequest request) {
        log.info("Running {} tests for runId={}", request.getTestCases().size(), request.getRunId());
        return ApiResponse.ok(testOrchestrationService.runTests(request));
    }

    @GetMapping("/results/{runId}")
    public ApiResponse<ResultsSummaryDto> getResults(@PathVariable String runId) {
        return ApiResponse.ok(testOrchestrationService.getResults(runId));
    }

    @GetMapping("/export/{runId}")
    public ResponseEntity<byte[]> exportResults(@PathVariable String runId) {
        List<TestResultDto> results = testOrchestrationService.getResultsForExport(runId);
        byte[] excelBytes = excelExportService.generateReport(results, runId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "test-report-" + runId + ".xlsx");
        headers.setContentLength(excelBytes.length);

        log.info("Exported Excel report for runId={}", runId);
        return ResponseEntity.ok().headers(headers).body(excelBytes);
    }
}
