package com.lunar.challenge.rockets.domain.event;

public record RocketMissionChanged(String newMission) implements RocketEvent {
}