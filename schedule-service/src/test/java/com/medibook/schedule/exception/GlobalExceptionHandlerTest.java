package com.medibook.schedule.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRuntimeException() {
        RuntimeException ex = new RuntimeException("test error");
        com.medibook.schedule.dto.ApiMessage response = handler.handleRuntimeException(ex);
        
        assertThat(response.getMessage()).isEqualTo("test error");
    }
}
