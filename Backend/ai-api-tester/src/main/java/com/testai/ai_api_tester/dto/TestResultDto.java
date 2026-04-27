package com.testai.ai_api_tester.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResultDto {

    private String testCaseId;
    private String name;
    private String endpoint;
    private String method;
    private Object payload;
    private Integer expectedStatus;
    private Integer actualStatus;
    private Object actualResponse;
    private Long responseTimeMs;
    private Boolean passed;
    private String errorMessage;
    private Boolean schemaValid;
}
