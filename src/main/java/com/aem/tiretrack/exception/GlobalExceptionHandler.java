package com.aem.tiretrack.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.aem.tiretrack.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", request, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {
        Map<String, String> errors = exception.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        violation -> violation.getMessage(),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new));

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", request, errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request) {
        log.warn("Bad request", exception);
        return buildResponse(HttpStatus.BAD_REQUEST, safeMessage(rootMessage(exception)), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request) {
        log.warn("Access denied", exception);
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "You do not have permission to access this resource.",
                request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(
            AuthenticationException exception,
            HttpServletRequest request) {
        log.warn("Authentication failed", exception);
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Authentication is required to access this resource.",
                request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateValues(
            DataIntegrityViolationException exception,
            HttpServletRequest request) {
        log.warn("Database constraint violation", exception);
        return buildResponse(
                HttpStatus.CONFLICT,
                "A record with this value already exists.",
                request);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ErrorResponse> handleTransactionErrors(
            TransactionSystemException exception,
            HttpServletRequest request) {
        log.warn("Persistence request failed", exception);
        ConstraintViolationException validationException = findConstraintViolation(exception);
        String message = validationException == null
                ? "The request could not be saved. Please check the details and try again."
                : validationException.getConstraintViolations().stream()
                        .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                        .collect(Collectors.joining(", "));

        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeErrors(
            RuntimeException exception,
            HttpServletRequest request) {
        log.warn("Request failed", exception);
        return buildResponse(HttpStatus.BAD_REQUEST, safeRuntimeMessage(exception), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralErrors(
            Exception exception,
            HttpServletRequest request) {
        log.error("Unexpected request failure", exception);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please contact support if the issue continues.",
                request);
    }

    private String safeRuntimeMessage(RuntimeException exception) {
        if (exception instanceof IllegalArgumentException) {
            return rootMessage(exception);
        }

        if (exception.getClass().equals(RuntimeException.class) && exception.getMessage() != null && !exception.getMessage().isBlank()) {
            return exception.getMessage();
        }

        String message = rootMessage(exception);
        return message == null || message.isBlank()
                ? "The request could not be completed. Please check the details and try again."
                : message;
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request) {
        return buildResponse(status, message, request, null);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> validationErrors) {
        ErrorResponse response = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                validationErrors);

        return ResponseEntity.status(status).body(response);
    }

    private String safeMessage(String message) {
        return message == null || message.isBlank()
                ? "The request could not be completed. Please check the details and try again."
                : message;
    }

    private ConstraintViolationException findConstraintViolation(Throwable throwable) {
        Throwable current = throwable;

        while (current != null) {
            if (current instanceof ConstraintViolationException validationException) {
                return validationException;
            }

            current = current.getCause();
        }

        return null;
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;

        while (current.getCause() != null) {
            if (current instanceof ConstraintViolationException validationException) {
                return validationException.getConstraintViolations().stream()
                        .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                        .collect(Collectors.joining(", "));
            }

            current = current.getCause();
        }

        if (current instanceof ConstraintViolationException validationException) {
            return validationException.getConstraintViolations().stream()
                    .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                    .collect(Collectors.joining(", "));
        }

        return current.getMessage() == null ? throwable.getMessage() : current.getMessage();
    }
}
