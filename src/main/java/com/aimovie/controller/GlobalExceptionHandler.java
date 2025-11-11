package com.aimovie.controller;

import com.aimovie.exception.AiServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "RUNTIME_ERROR");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "VALIDATION_ERROR");
        body.put("message", ex.getBindingResult().getFieldError() != null ? 
                ex.getBindingResult().getFieldError().getDefaultMessage() : "Validation error");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<Map<String, Object>> handleAiServiceException(AiServiceException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getErrorCode());
        body.put("service", ex.getServiceName());
        body.put("message", ex.getMessage());
        body.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, Object>> handleResourceAccessException(ResourceAccessException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "AI_SERVICE_UNAVAILABLE");
        body.put("message", "AI service is not accessible. Please check if the service is running on localhost:3000");
        body.put("details", ex.getMessage());
        body.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, Object>> handleHttpClientErrorException(HttpClientErrorException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "AI_SERVICE_CLIENT_ERROR");
        body.put("message", "AI service returned client error: " + ex.getStatusCode());
        body.put("details", ex.getResponseBodyAsString());
        body.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<Map<String, Object>> handleHttpServerErrorException(HttpServerErrorException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "AI_SERVICE_SERVER_ERROR");
        body.put("message", "AI service returned server error: " + ex.getStatusCode());
        body.put("details", ex.getResponseBodyAsString());
        body.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "INTERNAL_SERVER_ERROR");
        body.put("message", ex.getMessage());
        body.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
