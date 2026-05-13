package com.medibook.payment.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.medibook.payment.dto.ApiMessage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.RestClientException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRuntimeException_returnsMessage() {
        RuntimeException ex = new RuntimeException("Test error");
        ApiMessage response = handler.handleRuntimeException(ex);
        assertThat(response.getMessage()).isEqualTo("Test error");
    }

    @Test
    void handleRestClientException_returnsStandardMessage() {
        RestClientException ex = new RestClientException("Connection failed");
        ApiMessage response = handler.handleRestClientException(ex);
        assertThat(response.getMessage()).contains("Unable to communicate");
    }

    @Test
    void handleValidationException_returnsFirstErrorMessage() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "Bad field");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ApiMessage response = handler.handleValidationException(ex);
        assertThat(response.getMessage()).isEqualTo("Bad field");
    }
}
