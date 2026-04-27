package com.testai.ai_api_tester.controller;

import com.testai.ai_api_tester.dto.ApiResponse;
import com.testai.ai_api_tester.dto.InsightRequest;
import com.testai.ai_api_tester.dto.InsightResponse;
import com.testai.ai_api_tester.service.ClaudeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/insights")
@Slf4j
@RequiredArgsConstructor
public class InsightController {

    private final ClaudeService claudeService;

    @PostMapping("/explain")
    public ApiResponse<InsightResponse> explain(@Valid @RequestBody InsightRequest request) {
        log.info("Explaining failure for test '{}'", request.getTestCaseName());
        return ApiResponse.ok(claudeService.explainFailure(request));
    }
}
