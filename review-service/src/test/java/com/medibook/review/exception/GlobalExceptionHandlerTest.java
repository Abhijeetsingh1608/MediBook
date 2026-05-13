package com.medibook.review.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.medibook.review.dto.ApiMessage;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRuntimeException_returnsMessage() {
        RuntimeException ex = new RuntimeException("Error");
        ApiMessage message = handler.handleRuntimeException(ex);
        assertThat(message.getMessage()).isEqualTo("Error");
    }

    @Test
    void handleRestClientException_returnsStandardMessage() {
        RestClientException ex = new RestClientException("Error");
        ApiMessage message = handler.handleRestClientException(ex);
        assertThat(message.getMessage()).isEqualTo("Unable to communicate with required service");
    }
}
