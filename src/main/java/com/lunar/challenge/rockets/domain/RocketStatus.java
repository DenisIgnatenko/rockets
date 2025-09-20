package com.lunar.challenge.rockets.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class RocketStatus {
    private final String channel;
    private String type; //Falcon-9
    private int speed;
    private String mission; //"ARTEMIS"
    private boolean exploded;
    private OffsetDateTime lastMessageTime;

    public RocketStatus(String channel) {
        this.channel = channel;
    }
}
