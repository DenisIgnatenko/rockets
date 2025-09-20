package com.lunar.challenge.rockets.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;

/**
 * Data from incoming rocket messages
 */
@Getter
@Setter
@ToString
public class Metadata {
    private String channel;
    private int messageNumber;
    private OffsetDateTime messageTime;
    private String messageType;
}
