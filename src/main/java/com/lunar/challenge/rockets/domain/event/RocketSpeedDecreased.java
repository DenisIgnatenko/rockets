package com.lunar.challenge.rockets.domain.event;

public record RocketSpeedDecreased(int delta) implements RocketEvent {
}