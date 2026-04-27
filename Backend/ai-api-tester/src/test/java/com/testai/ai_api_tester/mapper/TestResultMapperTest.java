package com.testai.ai_api_tester.mapper;

import com.testai.ai_api_tester.dto.TestResultDto;
import com.testai.ai_api_tester.model.TestCase;
import com.testai.ai_api_tester.model.TestResult;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TestResultMapperTest {

    private final TestResultMapper mapper = new TestResultMapper();

    @Test
    void toDto_mapsTestCaseId_asString() {
        UUID id = UUID.randomUUID();
        TestResult result = TestResult.builder().testCaseId(id).responseTimeMs(0L).passed(true).build();

        TestResultDto dto = mapper.toDto(result, null);

        assertThat(dto.getTestCaseId()).isEqualTo(id.toString());
    }

    @Test
    void toDto_returnsNullTestCaseId_whenResultTestCaseIdIsNull() {
        TestResult result = TestResult.builder().testCaseId(null).responseTimeMs(0L).passed(true).build();

        TestResultDto dto = mapper.toDto(result, null);

        assertThat(dto.getTestCaseId()).isNull();
    }

    @Test
    void toDto_mapsAllFieldsFromTestCase_whenPresent() {
        UUID tcId = UUID.randomUUID();
        TestCase testCase = TestCase.builder()
                .id(tcId)
                .name("Create user")
                .endpoint("/api/users")
                .method("POST")
                .payload("{\"name\":\"Alice\"}")
                .expectedStatus(201)
                .build();

        TestResult result = TestResult.builder()
                .testCaseId(tcId)
                .actualStatus(201)
                .responseTimeMs(150L)
                .passed(true)
                .errorMessage(null)
                .build();

        TestResultDto dto = mapper.toDto(result, testCase);

        assertThat(dto.getName()).isEqualTo("Create user");
        assertThat(dto.getEndpoint()).isEqualTo("/api/users");
        assertThat(dto.getMethod()).isEqualTo("POST");
        assertThat(dto.getExpectedStatus()).isEqualTo(201);
        assertThat(dto.getActualStatus()).isEqualTo(201);
        assertThat(dto.getResponseTimeMs()).isEqualTo(150L);
        assertThat(dto.getPassed()).isTrue();
    }

    @Test
    void toDto_usesUnknownDefaults_whenTestCaseIsNull() {
        TestResult result = TestResult.builder()
                .testCaseId(UUID.randomUUID())
                .actualStatus(500)
                .responseTimeMs(0L)
                .passed(false)
                .build();

        TestResultDto dto = mapper.toDto(result, null);

        assertThat(dto.getName()).isEqualTo("Unknown");
        assertThat(dto.getEndpoint()).isEqualTo("Unknown");
        assertThat(dto.getMethod()).isEqualTo("GET");
        assertThat(dto.getExpectedStatus()).isNull();
        assertThat(dto.getPayload()).isNull();
    }

    @Test
    void toDto_mapsPassed_asFalse() {
        TestResult result = TestResult.builder()
                .passed(false)
                .responseTimeMs(0L)
                .build();

        TestResultDto dto = mapper.toDto(result, null);

        assertThat(dto.getPassed()).isFalse();
    }

    @Test
    void toDto_mapsErrorMessage_whenPresent() {
        TestResult result = TestResult.builder()
                .passed(false)
                .responseTimeMs(0L)
                .errorMessage("Connection timed out")
                .build();

        TestResultDto dto = mapper.toDto(result, null);

        assertThat(dto.getErrorMessage()).isEqualTo("Connection timed out");
    }
}
