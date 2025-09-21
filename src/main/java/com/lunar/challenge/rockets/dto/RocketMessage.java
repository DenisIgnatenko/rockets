package com.lunar.challenge.rockets.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

/**
 * Rocket message: metadata + payload
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RocketMessage {
    private Metadata metadata;

    @JsonProperty("message")
    private JsonNode payload;
}