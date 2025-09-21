package com.lunar.challenge.rockets.domain.event;

import com.lunar.challenge.rockets.domain.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

/**
 * Represents an already applied event with its effect on rocket state.
 */
public record AppliedEvent(
        OffsetDateTime time,
        MessageType type,
        int speedAfter
) {
}