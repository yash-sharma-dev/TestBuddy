package com.testai.ai_api_tester.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightRequest {

    private String testCaseName;
    private String endpoint;
    private String method;
    private Object payload;
    private Integer expectedStatus;
    private Integer actualStatus;
    private String errorMessage;
}
