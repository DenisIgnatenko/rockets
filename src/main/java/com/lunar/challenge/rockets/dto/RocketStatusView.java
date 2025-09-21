package com.lunar.challenge.rockets.dto;

import com.lunar.challenge.rockets.domain.event.AppliedEvent;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * View model for rocket status (exposed to API).
 */
@Getter
@Builder
@ToString
public class RocketStatusView {
    private final String channel;
    private final String type;
    private final int speed;
    private final String mission;
    private final boolean exploded;
    private final OffsetDateTime lastMessageTime;
    private List<AppliedEvent> history;
}