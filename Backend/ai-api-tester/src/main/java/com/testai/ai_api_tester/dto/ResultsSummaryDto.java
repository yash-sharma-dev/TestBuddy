package com.testai.ai_api_tester.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultsSummaryDto {

    private String runId;
    private List<TestResultDto> results;
    private Map<String, Object> summary;

    // Pagination metadata — null when not a paginated response (e.g. runTests, export)
    private Long totalElements;
    private Integer totalPages;
    private Integer currentPage;
    private Integer pageSize;
}
