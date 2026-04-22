package com.testai.ai_api_tester.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateRequest {

    private String runId;
    private String instructions;
    private String spec;
    private String targetBaseUrl;
    private String environment;
    private String authType;
    private String authValue;
}
