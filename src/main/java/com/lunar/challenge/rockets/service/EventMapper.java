package com.lunar.challenge.rockets.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.lunar.challenge.rockets.domain.MessageType;
import com.lunar.challenge.rockets.domain.RocketMessageFields;
import com.lunar.challenge.rockets.domain.event.*;
import com.lunar.challenge.rockets.utils.JsonUtils;

public class EventMapper {
    public static RocketEvent from(MessageType type, JsonNode payload) {
        return switch (type) {
            case RocketLaunched -> new RocketLaunched(
                    JsonUtils.getText(payload, RocketMessageFields.TYPE, "Unknown"),
                    JsonUtils.getText(payload, RocketMessageFields.MISSION, "Unknown"),
                    JsonUtils.getInt(payload, RocketMessageFields.LAUNCH_SPEED, 0)
            );
            case RocketSpeedIncreased -> new RocketSpeedIncreased(
                    JsonUtils.getInt(payload, RocketMessageFields.BY, 0)
            );
            case RocketSpeedDecreased -> new RocketSpeedDecreased(
                    JsonUtils.getInt(payload, RocketMessageFields.BY, 0)
            );
            case RocketMissionChanged -> new RocketMissionChanged(
                    JsonUtils.getText(payload, RocketMessageFields.NEW_MISSION, "Unknown")
            );
            case RocketExploded -> new RocketExploded();
        };
    }
}