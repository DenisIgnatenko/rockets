package com.lunar.challenge.rockets.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Rocket message: metadata + payload
 */
@Getter
@Setter
@ToString
public class RocketMessage {
    private Metadata metadata;
    private JsonNode message;
}
