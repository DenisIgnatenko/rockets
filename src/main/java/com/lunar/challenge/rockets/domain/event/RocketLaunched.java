package com.lunar.challenge.rockets.domain.event;

public record RocketLaunched(String type, String mission, int initialSpeed) implements RocketEvent {
}