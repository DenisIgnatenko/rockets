package com.lunar.challenge.rockets.domain.event;

public sealed interface RocketEvent
        permits RocketLaunched, RocketSpeedIncreased, RocketSpeedDecreased,
        RocketMissionChanged, RocketExploded {
}