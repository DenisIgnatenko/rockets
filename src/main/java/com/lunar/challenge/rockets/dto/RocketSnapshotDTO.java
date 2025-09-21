package com.lunar.challenge.rockets.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;


/**
 * DTO for Redis
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RocketSnapshotDTO {
    private String channel;
    private String type;
    private int speed;
    private String mission;
    private boolean exploded;
    private OffsetDateTime lastMessageTime;
    private List<RocketHistoryItem> history;
}