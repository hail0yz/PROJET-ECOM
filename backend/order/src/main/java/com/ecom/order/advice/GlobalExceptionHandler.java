package com.ecom.order.advice;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
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
import com.ecom.order.exception.OrderAlreadyExistsException;
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
                .error("ENTITY_NOT_FOUND")
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(OrderAlreadyExistsException.class)
    public ResponseEntity<APIErrorResponse> handle(OrderAlreadyExistsException ex) {
        HttpStatus status = HttpStatus.CONFLICT;
        APIErrorResponse errorResponse = APIErrorResponse.builder()
                .status(status.value())
                .error("ORDER_ALREADY_EXISTS")
                .message(ex.getMessage())
                .details(new OrderId(ex.getOrderId()))
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(BadREquestException.class)
    public ResponseEntity<APIErrorResponse> handle(BadREquestException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        APIErrorResponse errorResponse = APIErrorResponse.builder()
                .status(status.value())
                .error("BAD_REQUEST")
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<APIErrorResponse> handle(ExternalServiceException ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        APIErrorResponse errorResponse = APIErrorResponse.builder()
                .status(status.value())
                .error("INTERNAL_SERVER_ERROR")
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
                .error("REQUEST_BODY_INVALID")
                .message("Validation failed : " + ex.getMessage())
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
                .error("REQUEST_PARAMETERS_INVALID")
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
                .error("REQUEST_BODY_INVALID")
                .message("Constraint violation : " + e.getMessage())
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<APIErrorResponse> handle(IllegalArgumentException e) {
        APIErrorResponse error = APIErrorResponse.builder()
                .message(e.getMessage())
                .status(BAD_REQUEST.value())
                .error("ILLEGAL_ARGUMENT")
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<APIErrorResponse> handle(IllegalStateException e) {
        APIErrorResponse error = APIErrorResponse.builder()
                .error("ILLEGAL_STATE")
                .status(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
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

    private record OrderId(UUID orderId) {}

}
