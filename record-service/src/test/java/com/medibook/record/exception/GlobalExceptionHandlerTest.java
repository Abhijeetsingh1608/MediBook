package com.medibook.record.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.medibook.record.dto.ApiMessage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.RestClientException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRuntimeException_returnsOriginalMessage() {
        ApiMessage response = handler.handleRuntimeException(new RuntimeException("Bad request"));

        assertThat(response.getMessage()).isEqualTo("Bad request");
    }

    @Test
    void handleValidationException_returnsFirstValidationMessage() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("obj", "field", "Required")));

        ApiMessage response = handler.handleValidationException(exception);

        assertThat(response.getMessage()).isEqualTo("Required");
    }

    @Test
    void handleRestClientException_returnsStandardMessage() {
        ApiMessage response = handler.handleRestClientException(new RestClientException("timeout"));

        assertThat(response.getMessage()).isEqualTo("Unable to communicate with required service");
    }
}
