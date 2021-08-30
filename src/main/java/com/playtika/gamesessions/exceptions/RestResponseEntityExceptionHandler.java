package com.playtika.gamesessions.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.naming.AuthenticationException;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {IllegalArgumentException.class})
    protected ResponseEntity<Object> handleNotFound(
            RuntimeException ex, WebRequest request) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {IllegalStateException.class})
    protected ResponseEntity<Object> handleStateException(
            RuntimeException ex, WebRequest request) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {AuthenticationException.class})
    protected ResponseEntity<Object> handleAuthentication(
            RuntimeException ex, WebRequest request) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NETWORK_AUTHENTICATION_REQUIRED);
    }

    @ExceptionHandler(value ={OverAllocatedTimeException.class})
    protected ResponseEntity<Object> handleOvertime(
            RuntimeException ex, WebRequest request) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CREATED);
    }
}
