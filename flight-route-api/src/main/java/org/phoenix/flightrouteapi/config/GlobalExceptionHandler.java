package org.phoenix.flightrouteapi.config;

import org.phoenix.flightrouteapi.shared.exceptions.DomainException;
import org.phoenix.flightrouteapi.shared.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("Validation error: {}", ex.getMessage());
        var errors = ex.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        pd.setTitle("Validation Error");
        pd.setProperty("errors", errors);
        pd.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handle(ResourceNotFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        pd.setTitle("Resource Not Found");
        pd.setProperty("errors", List.of(e.getMessage()));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handle(DomainException e) {
        log.warn("Domain exception: {}", e.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        pd.setTitle("Conflict");
        pd.setProperty("errors", List.of(e.getMessage()));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handle(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Access denied");
        pd.setTitle("Forbidden");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception e) {
        log.error("Unexpected exception", e);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}