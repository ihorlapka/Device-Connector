package com.iot.command_control_service.controller.errors;

import lombok.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CommandNotSentException.class)
    public ResponseEntity<ErrorResponse> handleCommandNotSentException(CommandNotSentException ex, WebRequest request) {
        final ErrorResponse response = ErrorResponse.of(
                NOT_FOUND,
                ex.getMessage(),
                "Unable to send command!",
                URI.create(request.getDescription(false)),
                emptyMap());
        return new ResponseEntity<>(response, NOT_FOUND);
    }

    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity<ErrorResponse> handlePermissionDeniedException(PermissionDeniedException ex, WebRequest request) {
        final ErrorResponse response = ErrorResponse.of(
                FORBIDDEN,
                ex.getMessage(),
                "Permission denied!",
                URI.create(request.getDescription(false)),
                emptyMap());
        return new ResponseEntity<>(response, FORBIDDEN);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status, WebRequest request) {
        final Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        final ErrorResponse response = ErrorResponse.of(
                BAD_REQUEST,
                ex.getMessage(),
                "Validation failed for one or more fields!",
                URI.create(request.getDescription(false)),
                errors);
        return new ResponseEntity<>(response, BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex, WebRequest request) {
        logger.error("An unexpected error occurred: " + ex.getMessage(), ex);

        final ErrorResponse response = ErrorResponse.of(
                INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                "Server error!",
                URI.create(request.getDescription(false)),
                emptyMap());
        return new ResponseEntity<>(response, INTERNAL_SERVER_ERROR);
    }
}
