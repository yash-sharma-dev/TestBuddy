package com.testai.ai_api_tester.controller;

import com.testai.ai_api_tester.dto.ApiResponse;
import com.testai.ai_api_tester.dto.InsightRequest;
import com.testai.ai_api_tester.dto.InsightResponse;
import com.testai.ai_api_tester.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/insights")
@CrossOrigin(origins = "*")
@Slf4j
@RequiredArgsConstructor
public class InsightController {

    private final GeminiService geminiService;

    @PostMapping("/explain")
    public ApiResponse<InsightResponse> explain(@RequestBody InsightRequest request) {
        try {
            log.info("Explaining failure for test '{}'", request.getTestCaseName());
            InsightResponse insight = geminiService.explainFailure(request);
            return ApiResponse.ok(insight);
        } catch (Exception e) {
            log.error("Failed to generate insight", e);
            return ApiResponse.error("Failed to generate insight: " + e.getMessage());
        }
    }
}
