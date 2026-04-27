package com.testai.ai_api_tester.controller.advice;

import com.testai.ai_api_tester.dto.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_returnsFailureResponse_withExceptionMessage() {
        EntityNotFoundException ex = new EntityNotFoundException("Run not found: abc");

        ApiResponse<Void> response = handler.handleNotFound(ex);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getError()).isEqualTo("Run not found: abc");
    }

    @Test
    void handleIllegalArgument_returnsFailureResponse_withExceptionMessage() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid UUID format");

        ApiResponse<Void> response = handler.handleIllegalArgument(ex);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getError()).isEqualTo("Invalid UUID format");
    }

    @Test
    void handleGeneral_returnsGenericMessage_andDoesNotLeakDetails() {
        RuntimeException ex = new RuntimeException("Internal DB password is hunter2");

        ApiResponse<Void> response = handler.handleGeneral(ex);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getError())
                .doesNotContain("hunter2")
                .isEqualTo("An internal error occurred. Please try again.");
    }
}
