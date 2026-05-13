package com.medibook.auth.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.medibook.auth.dto.ApiMessage;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRuntimeException_returnsBadRequest() {
        RuntimeException ex = new RuntimeException("Test error");
        ResponseEntity<ApiMessage> response = handler.handleRuntimeException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Test error");
    }

    @Test
    void handleGenericException_returnsInternalServerError() {
        Exception ex = new Exception("Critical failure");
        ResponseEntity<ApiMessage> response = handler.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).isEqualTo("Something went wrong");
    }
}
