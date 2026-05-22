package com.aem.tiretrack.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationErrors(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        return errors;
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleRuntimeErrors(RuntimeException exception) {
        log.warn("Request failed", exception);
        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("message", safeRuntimeMessage(exception));
        return errors;
    }

    @ExceptionHandler({TransactionSystemException.class, DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handlePersistenceErrors(Exception exception) {
        log.warn("Persistence request failed", exception);
        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("message", "The request could not be saved. Please check the details and try again.");
        return errors;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleGeneralErrors(Exception exception) {
        log.error("Unexpected request failure", exception);
        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("message", "Something went wrong. Please try again.");
        return errors;
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
