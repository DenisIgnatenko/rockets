package com.lunar.challenge.rockets.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.List;

@Value
@Builder
@Schema(description = "Error response")
public class ErrorResponse {
    @Schema(description = "Error message",
            example = "Rocket with channel 193270a9-c9cf-404a-8f83-838e71d9ae67 not found")
    String message;

    @Schema(description = "HTTP status code", example = "404")
    int status;

    @Schema(description = "Timestamp of the error", example = "2025-09-19T15:40:00Z")
    OffsetDateTime timestamp;

    @Schema(description = "List of validation or detailed error messages",
            example = "[\"channel must not be null\", \"messageNumber must be greater than 0\"] etc")
    List<String> errors;
}