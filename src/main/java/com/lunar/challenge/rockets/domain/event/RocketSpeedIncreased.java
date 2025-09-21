package com.lunar.challenge.rockets.domain.event;

public record RocketSpeedIncreased(int delta) implements RocketEvent {
}