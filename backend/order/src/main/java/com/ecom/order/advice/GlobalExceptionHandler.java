package com.ecom.order.advice;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.ecom.order.exception.BadREquestException;
import com.ecom.order.exception.BusinessException;
import com.ecom.order.exception.EntityNotFoundException;
import com.ecom.order.exception.ExternalServiceException;
import lombok.Builder;
import lombok.Value;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<APIErrorResponse> handle(EntityNotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        APIErrorResponse errorResponse = APIErrorResponse.builder()
                .status(status.value())
                .error(ex.getMessage())
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(BadREquestException.class)
    public ResponseEntity<APIErrorResponse> handle(BadREquestException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        APIErrorResponse errorResponse = APIErrorResponse.builder()
                .status(status.value())
                .error(ex.getMessage())
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<APIErrorResponse> handle(ExternalServiceException ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        APIErrorResponse errorResponse = APIErrorResponse.builder()
                .status(status.value())
                .error(ex.getMessage())
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIErrorResponse> handle(MethodArgumentNotValidException ex) {
        Map<String, String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> Objects.requireNonNullElse(error.getDefaultMessage(), "Invalid value"),
                        (a, b) -> a
                ));

        APIErrorResponse errorResponse = APIErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(ex.getMessage())
                .message("Validation failed")
                .details(details)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<APIErrorResponse> handle(MethodArgumentTypeMismatchException ex) {
        Map<String, String> details = Map.of(
                ex.getName(), "Invalid value: " + ex.getValue() + " for type " + ex.getRequiredType().getSimpleName()
        );

        APIErrorResponse errorResponse = APIErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(ex.getMessage())
                .message("Path variable or request parameter type mismatch")
                .details(details)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<APIErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        Map<String, String> violations = e.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (a, b) -> a
                ));

        var error = APIErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(e.getMessage())
                .message("Constraint violation")
                .details(violations)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<APIErrorResponse> handle(BusinessException e) {
        var error = APIErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(e.getMessage())
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(BAD_REQUEST).body(error);
    }

    @Value
    @Builder
    public static class APIErrorResponse {

        int status;

        String error;

        String message;

        @Builder.Default
        Instant timestamp = Instant.now();

        Object details;

    }

}
