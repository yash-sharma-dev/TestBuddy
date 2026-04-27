package com.testai.ai_api_tester.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunTestsRequest {

    @NotBlank(message = "runId is required")
    private String runId;

    @NotEmpty(message = "testCases must not be empty")
    private List<TestCaseDto> testCases;

    @NotBlank(message = "targetBaseUrl is required")
    private String targetBaseUrl;

    private String authType;
    private String authValue;
}
