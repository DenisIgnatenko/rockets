package com.lunar.challenge.rockets.dto;

import lombok.*;

import java.time.OffsetDateTime;

/**
 * Data from incoming rocket messages
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Metadata {
    private String channel;

    private int messageNumber;

    private OffsetDateTime messageTime;

    private String messageType;
}