package com.medibook.notification.exception;

import com.medibook.notification.dto.ApiMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
/*
 * This class handles exceptions for GlobalExceptionHandler.
 * Instead of showing raw errors, it sends proper error responses to client.
 */
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    public ApiMessage handleRuntimeException(RuntimeException ex) {
        return ApiMessage.builder()
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    public ApiMessage handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Invalid request");

        return ApiMessage.builder()
                .message(message)
                .build();
    }
}
