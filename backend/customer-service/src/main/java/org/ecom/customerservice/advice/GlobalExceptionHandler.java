package org.ecom.customerservice.advice;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.ecom.customerservice.dto.APIErrorResponse;
import org.ecom.customerservice.exception.EmailAlreadyExistsException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<APIErrorResponse> handle(EmailAlreadyExistsException e) {
        APIErrorResponse error = APIErrorResponse.builder()
                .error(e.getMessage())
                .status(HttpStatus.CONFLICT.value())
                .message("Email already exists")
                .build();

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
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

}
