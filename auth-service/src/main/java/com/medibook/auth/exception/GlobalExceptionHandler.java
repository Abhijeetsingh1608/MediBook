package com.medibook.auth.exception;

import com.medibook.auth.dto.ApiMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
/*
 * This class handles exceptions for GlobalExceptionHandler.
 * Instead of showing raw errors, it sends proper error responses to client.
 */
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    public ResponseEntity<ApiMessage> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiMessage.builder()
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(Exception.class)
/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    public ResponseEntity<ApiMessage> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiMessage.builder()
                        .message("Something went wrong")
                        .build());
    }
}
