package com.lunar.challenge.rockets.dto;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

/**
 * DTO for API responses
 *
 * @Value makes DTO immutableXnj
 */
@Value
@Builder
public class RocketStatusView {
    String channel;
    String type;
    int speed;
    String mission;
    boolean exploded;
    OffsetDateTime lastMessageTime;
}
