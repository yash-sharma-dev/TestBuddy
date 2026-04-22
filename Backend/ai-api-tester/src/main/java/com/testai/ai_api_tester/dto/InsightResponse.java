package com.testai.ai_api_tester.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightResponse {

    private String technical;
    private String human;
    private String suggestion;
}
