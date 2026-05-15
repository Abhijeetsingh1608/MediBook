package com.medibook.provider.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
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
    public Map<String, String> handleRuntimeException(RuntimeException ex) {
        ex.printStackTrace();
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    public Map<String, String> handleException(Exception ex) {
        ex.printStackTrace();
        return Map.of("message", "Something went wrong");
    }
}
