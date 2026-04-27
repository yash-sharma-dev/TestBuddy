package com.testai.ai_api_tester.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "test_results")
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "test_case_id")
    private UUID testCaseId;

    @Column(name = "run_id")
    private UUID runId;

    @Column(name = "actual_status")
    private Integer actualStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "actual_response", columnDefinition = "jsonb")
    private String actualResponse;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    private Boolean passed;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "schema_valid")
    private Boolean schemaValid;

    @Column(name = "executed_at")
    private OffsetDateTime executedAt;

    @PrePersist
    public void prePersist() {
        if (executedAt == null) {
            executedAt = OffsetDateTime.now();
        }
    }
}
