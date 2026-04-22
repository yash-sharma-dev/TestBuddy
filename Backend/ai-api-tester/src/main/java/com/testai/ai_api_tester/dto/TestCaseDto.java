package com.testai.ai_api_tester.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestCaseDto {

    private String id;
    private String name;
    private String endpoint;
    private String method;
    private Map<String, String> headers;
    private Object payload;
    private Integer expectedStatus;
    private Object expectedSchema;
    private String testType;     // happy, negative, edge
    private String chainFrom;    // nullable — ID of test to chain from
    private String description;
}
