package com.ecom.bookService.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "Standard error response for API errors")
public class APIErrorResponse {

    @Schema(description = "HTTP status code", example = "400")
    int status;

    @Schema(description = "Short error message", example = "Bad Request")
    String error;

    @Schema(description = "Detailed error message", example = "Email format is invalid")
    String message;

    @Schema(description = "Timestamp when the error occurred", example = "2025-10-26T12:34:56Z")
    @Builder.Default
    Instant timestamp = Instant.now();

    @Schema(description = "Additional structured details depending on the error type",
            example = "{\"email\": \"already registered\"}")
    Object details;

}