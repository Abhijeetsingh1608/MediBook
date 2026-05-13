package com.medibook.notification.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.medibook.notification.dto.ApiMessage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRuntimeException_returnsMessage() {
        ApiMessage response = handler.handleRuntimeException(new RuntimeException("Bad request"));

        assertThat(response.getMessage()).isEqualTo("Bad request");
    }

    @Test
    void handleValidationException_returnsFirstFieldMessage() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("obj", "field", "Required")));

        ApiMessage response = handler.handleValidationException(exception);

        assertThat(response.getMessage()).isEqualTo("Required");
    }
}
