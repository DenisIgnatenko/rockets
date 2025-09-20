package com.lunar.challenge.rockets.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

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
}