package com.testai.ai_api_tester.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testai.ai_api_tester.dto.*;
import com.testai.ai_api_tester.mapper.TestResultMapper;
import com.testai.ai_api_tester.model.TestCase;
import com.testai.ai_api_tester.model.TestResult;
import com.testai.ai_api_tester.model.TestRun;
import com.testai.ai_api_tester.repository.TestCaseRepository;
import com.testai.ai_api_tester.repository.TestResultRepository;
import com.testai.ai_api_tester.repository.TestRunRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestOrchestrationServiceTest {

    @Mock private ClaudeService claudeService;
    @Mock private SpecParserService specParserService;
    @Mock private TestExecutorService testExecutorService;
    @Mock private TestRunRepository testRunRepository;
    @Mock private TestCaseRepository testCaseRepository;
    @Mock private TestResultRepository testResultRepository;
    @Mock private TestResultMapper testResultMapper;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks private TestOrchestrationService service;

    // ── generateAndSaveTests ─────────────────────────────────────────────────

    @Test
    void generateAndSaveTests_throwsEntityNotFoundException_whenRunNotFound() {
        when(testRunRepository.findById(any())).thenReturn(Optional.empty());

        GenerateRequest request = GenerateRequest.builder()
                .runId(UUID.randomUUID().toString())
                .spec("openapi: 3.0.0")
                .targetBaseUrl("http://localhost")
                .build();

        assertThatThrownBy(() -> service.generateAndSaveTests(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Run not found");
    }

    @Test
    void generateAndSaveTests_throwsIllegalArgumentException_onInvalidRunId() {
        GenerateRequest request = GenerateRequest.builder()
                .runId("not-a-valid-uuid")
                .spec("openapi: 3.0.0")
                .targetBaseUrl("http://localhost")
                .build();

        assertThatThrownBy(() -> service.generateAndSaveTests(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generateAndSaveTests_setsRunStatus_toTestsGenerated() throws Exception {
        UUID runId = UUID.randomUUID();
        TestRun run = TestRun.builder().id(runId).status("SPEC_UPLOADED").build();

        when(testRunRepository.findById(runId)).thenReturn(Optional.of(run));
        when(specParserService.parseSpecContent(any())).thenReturn(java.util.Map.of());
        when(claudeService.generateTestCases(any(), any())).thenReturn(List.of());
        when(testCaseRepository.saveAll(any())).thenReturn(List.of());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        GenerateRequest request = GenerateRequest.builder()
                .runId(runId.toString())
                .spec("openapi: 3.0.0")
                .instructions("focus on auth")
                .targetBaseUrl("http://localhost")
                .build();

        service.generateAndSaveTests(request);

        ArgumentCaptor<TestRun> runCaptor = ArgumentCaptor.forClass(TestRun.class);
        verify(testRunRepository).save(runCaptor.capture());
        assertThat(runCaptor.getValue().getStatus()).isEqualTo("TESTS_GENERATED");
        assertThat(runCaptor.getValue().getInstructions()).isEqualTo("focus on auth");
    }

    @Test
    void generateAndSaveTests_savesTestCases_andBackfillsIds() throws Exception {
        UUID runId = UUID.randomUUID();
        UUID savedTcId = UUID.randomUUID();
        TestRun run = TestRun.builder().id(runId).status("SPEC_UPLOADED").build();
        TestCaseDto dto = new TestCaseDto();
        dto.setName("happy path");
        TestCase savedEntity = TestCase.builder().id(savedTcId).build();

        when(testRunRepository.findById(runId)).thenReturn(Optional.of(run));
        when(specParserService.parseSpecContent(any())).thenReturn(java.util.Map.of());
        when(claudeService.generateTestCases(any(), any())).thenReturn(List.of(dto));
        when(testCaseRepository.saveAll(any())).thenReturn(List.of(savedEntity));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        GenerateRequest request = GenerateRequest.builder()
                .runId(runId.toString())
                .spec("openapi: 3.0.0")
                .targetBaseUrl("http://localhost")
                .build();

        List<TestCaseDto> result = service.generateAndSaveTests(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(savedTcId.toString());
    }

    // ── runTests ─────────────────────────────────────────────────────────────

    @Test
    void runTests_setsRunStatus_toRunning() {
        UUID runId = UUID.randomUUID();
        TestRun run = TestRun.builder().id(runId).status("TESTS_GENERATED").build();
        when(testRunRepository.findById(runId)).thenReturn(Optional.of(run));
        when(testExecutorService.executeTests(any(), any(), any(), any(), any())).thenReturn(List.of());

        RunTestsRequest request = RunTestsRequest.builder()
                .runId(runId.toString())
                .testCases(List.of())
                .targetBaseUrl("http://localhost")
                .build();

        service.runTests(request);

        ArgumentCaptor<TestRun> captor = ArgumentCaptor.forClass(TestRun.class);
        verify(testRunRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("RUNNING");
    }

    @Test
    void runTests_buildsSummaryWithCorrectCounts() {
        UUID runId = UUID.randomUUID();
        when(testRunRepository.findById(runId)).thenReturn(Optional.empty());

        TestResultDto passedDto = TestResultDto.builder().passed(true).responseTimeMs(100L).build();
        TestResultDto failedDto = TestResultDto.builder().passed(false).responseTimeMs(200L).build();
        when(testExecutorService.executeTests(any(), any(), any(), any(), any()))
                .thenReturn(List.of(passedDto, failedDto));

        RunTestsRequest request = RunTestsRequest.builder()
                .runId(runId.toString())
                .testCases(List.of())
                .targetBaseUrl("http://localhost")
                .build();

        ResultsSummaryDto result = service.runTests(request);

        assertThat(result.getSummary()).containsEntry("total", 2);
        assertThat(result.getSummary()).containsEntry("passed", 1L);
        assertThat(result.getSummary()).containsEntry("failed", 1L);
    }

    // ── getResults ───────────────────────────────────────────────────────────

    @Test
    void getResults_throwsIllegalArgumentException_onInvalidRunId() {
        assertThatThrownBy(() -> service.getResults("bad-uuid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getResults_returnsEmptySummary_whenNoResultsExist() {
        UUID runId = UUID.randomUUID();
        when(testResultRepository.findByRunId(runId)).thenReturn(List.of());
        when(testCaseRepository.findByRunId(runId)).thenReturn(List.of());

        ResultsSummaryDto result = service.getResults(runId.toString());

        assertThat(result.getResults()).isEmpty();
        assertThat(result.getSummary()).containsEntry("total", 0);
        assertThat(result.getSummary()).containsEntry("passed", 0L);
    }

    @Test
    void getResults_calculatesAverageResponseTime() {
        UUID runId = UUID.randomUUID();
        TestResult r1 = TestResult.builder().passed(true).responseTimeMs(100L).build();
        TestResult r2 = TestResult.builder().passed(true).responseTimeMs(300L).build();

        when(testResultRepository.findByRunId(runId)).thenReturn(List.of(r1, r2));
        when(testCaseRepository.findByRunId(runId)).thenReturn(List.of());
        when(testResultMapper.toDto(eq(r1), any()))
                .thenReturn(TestResultDto.builder().passed(true).responseTimeMs(100L).build());
        when(testResultMapper.toDto(eq(r2), any()))
                .thenReturn(TestResultDto.builder().passed(true).responseTimeMs(300L).build());

        ResultsSummaryDto result = service.getResults(runId.toString());

        assertThat(result.getSummary()).containsEntry("avgResponseTime", 200L);
    }

    // ── getResultsForExport ──────────────────────────────────────────────────

    @Test
    void getResultsForExport_returnsEmptyList_whenNoResultsExist() {
        UUID runId = UUID.randomUUID();
        when(testResultRepository.findByRunId(runId)).thenReturn(List.of());
        when(testCaseRepository.findByRunId(runId)).thenReturn(List.of());

        List<TestResultDto> results = service.getResultsForExport(runId.toString());

        assertThat(results).isEmpty();
    }

    @Test
    void getResultsForExport_delegatesMapping_toTestResultMapper() {
        UUID runId = UUID.randomUUID();
        TestResult result = TestResult.builder().passed(true).responseTimeMs(50L).build();
        TestResultDto dto = TestResultDto.builder().passed(true).responseTimeMs(50L).build();

        when(testResultRepository.findByRunId(runId)).thenReturn(List.of(result));
        when(testCaseRepository.findByRunId(runId)).thenReturn(List.of());
        when(testResultMapper.toDto(eq(result), any())).thenReturn(dto);

        List<TestResultDto> results = service.getResultsForExport(runId.toString());

        assertThat(results).containsExactly(dto);
        verify(testResultMapper).toDto(eq(result), any());
    }
}
