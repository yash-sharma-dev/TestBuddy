package com.testai.ai_api_tester.dto;

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

    private String runId;
    private List<TestCaseDto> testCases;
    private String targetBaseUrl;
    private String authType;
    private String authValue;
}
