package com.testai.ai_api_tester.mapper;

import com.testai.ai_api_tester.dto.TestResultDto;
import com.testai.ai_api_tester.model.TestCase;
import com.testai.ai_api_tester.model.TestResult;
import org.springframework.stereotype.Component;

@Component
public class TestResultMapper {

    public TestResultDto toDto(TestResult result, TestCase testCase) {
        return TestResultDto.builder()
                .testCaseId(result.getTestCaseId() != null ? result.getTestCaseId().toString() : null)
                .name(testCase != null ? testCase.getName() : "Unknown")
                .endpoint(testCase != null ? testCase.getEndpoint() : "Unknown")
                .method(testCase != null ? testCase.getMethod() : "GET")
                .payload(testCase != null ? testCase.getPayload() : null)
                .expectedStatus(testCase != null ? testCase.getExpectedStatus() : null)
                .actualStatus(result.getActualStatus())
                .actualResponse(result.getActualResponse())
                .responseTimeMs(result.getResponseTimeMs())
                .passed(result.getPassed())
                .errorMessage(result.getErrorMessage())
                .build();
    }
}
