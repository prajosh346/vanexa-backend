package com.vanexa.backend.exception;

import jakarta.validation.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            String message,
            Object details) {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        if (details != null) {
            body.put("details", details);
        }

        return ResponseEntity.status(status).body(body);
    }

    // Invalid DTO fields, such as a blank title
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException exception) {

        Map<String, String> fieldErrors =
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .collect(Collectors.toMap(
                                error -> error.getField(),
                                error -> error.getDefaultMessage() == null
                                        ? "Invalid value"
                                        : error.getDefaultMessage(),
                                (first, second) -> first,
                                LinkedHashMap::new
                        ));

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                fieldErrors
        );
    }

    // Invalid method parameters
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            ConstraintViolationException exception) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid request parameters",
                exception.getMessage()
        );
    }

    // Malformed JSON request body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidJson(
            HttpMessageNotReadableException exception) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid or malformed JSON request body",
                null
        );
    }

    // Missing required query parameter
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameter(
            MissingServletRequestParameterException exception) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Missing required parameter: "
                        + exception.getParameterName(),
                null
        );
    }

    // Duplicate database values, such as unique slugs
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(
            DataIntegrityViolationException exception) {

        return buildResponse(
                HttpStatus.CONFLICT,
                "The operation conflicts with existing database data",
                null
        );
    }

    // Handle existing service methods that throw IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException exception) {

        String message = exception.getMessage() == null
                ? "Invalid request"
                : exception.getMessage();

        String normalized = message.toLowerCase();

        if (normalized.contains("not found")) {
            return buildResponse(
                    HttpStatus.NOT_FOUND,
                    message,
                    null
            );
        }

        if (normalized.contains("already exists")
                || normalized.contains("duplicate")) {
            return buildResponse(
                    HttpStatus.CONFLICT,
                    message,
                    null
            );
        }

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                message,
                null
        );
    }

    // Unexpected server errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(
            Exception exception) {

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred",
                null
        );
    }
}