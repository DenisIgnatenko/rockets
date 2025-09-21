package com.lunar.challenge.rockets.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RocketHistoryItem {
    private OffsetDateTime time;
    private String type;
    private int speedAfter;
}