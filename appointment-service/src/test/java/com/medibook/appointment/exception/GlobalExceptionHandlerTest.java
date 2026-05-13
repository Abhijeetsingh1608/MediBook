package com.medibook.appointment.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.medibook.appointment.dto.ApiMessage;
import org.junit.jupiter.api.Test;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRuntimeException() {
        RuntimeException ex = new RuntimeException("test error");
        ApiMessage response = handler.handleRuntimeException(ex);
        assertThat(response.getMessage()).isEqualTo("test error");
    }

    @Test
    void handleRestClientException() {
        org.springframework.web.client.RestClientException ex = new org.springframework.web.client.RestClientException("Error");
        ApiMessage response = handler.handleRestClientException(ex);
        assertThat(response.getMessage()).isEqualTo("Unable to communicate with schedule service");
    }
}
