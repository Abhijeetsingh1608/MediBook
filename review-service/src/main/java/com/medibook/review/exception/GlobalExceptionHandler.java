package com.medibook.review.exception;

import com.medibook.review.dto.ApiMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

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

    @ExceptionHandler(RestClientException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    public ApiMessage handleRestClientException(RestClientException ex) {
        return ApiMessage.builder()
                .message("Unable to communicate with required service")
                .build();
    }
}
