package com.ecom.bookService.advice;

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

import com.ecom.bookService.dto.APIErrorResponse;
import com.ecom.bookService.exception.EntityNotFoundException;
import com.ecom.bookService.exception.ImageUploadFailedException;
import com.ecom.bookService.exception.InsufficientAvailableStockException;

@RestControllerAdvice
public class GlobalExceptionHandler {

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

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<APIErrorResponse> handle(EntityNotFoundException e) {
        var error = APIErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error(e.getMessage())
                .message(e.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InsufficientAvailableStockException.class)
    public ResponseEntity<APIErrorResponse> handle(InsufficientAvailableStockException e) {
        var error = APIErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .error(e.getMessage())
                .message(e.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<APIErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        APIErrorResponse error = APIErrorResponse.builder()
                .error("ILLEGAL_ARGUMENT")
                .status(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
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

    @ExceptionHandler(ImageUploadFailedException.class)
    public ResponseEntity<APIErrorResponse> handle(ImageUploadFailedException e) {
        APIErrorResponse error = APIErrorResponse.builder()
                .error("IMAGE_UPLOAD_FAILED")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(e.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}