package com.testai.ai_api_tester.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateRequest {

    @NotBlank(message = "runId is required")
    private String runId;

    private String instructions;

    @NotBlank(message = "spec content is required")
    private String spec;

    @NotBlank(message = "targetBaseUrl is required")
    private String targetBaseUrl;

    private String environment;
    private String authType;
    private String authValue;
}
