package com.lunar.challenge.rockets.domain;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Rocket Tracker
 * - receives incoming messages
 * - applies them to RocketStatus in right order
 * - allocates space for missing messages
 * - implements thread-safety with reentrant lock
 */
@Slf4j
public class RocketTracker {
    private final ReentrantLock lock = new ReentrantLock();

    @Getter
    private final RocketStatus status;

    public String getChannel() {
        return status.getChannel();
    }

    //volatile to guarantee that reading will always be from memory, rather that from
    //local thread cache
    private volatile int lastApplied = 0; //last applied message number;

    //creating immutable DTO for temporarily store pending messages
    // + concurrency support: once created - opened only for read
    private record PendingMessage(int messageNumber,
                                  OffsetDateTime time,
                                  MessageType type,
                                  JsonNode payload
    ) {
    }


    //we need the structure that can store entries sorted by the messageNubmer (key)
    //in hashmap keys are unsorted, harder to search for next
    //arraylist if we receive messageNumber 1000, we will have to create list with 1001 elements (999 empty)
    //- uneffective by memory and time.
    //Treemap - good choise.
    //NavigableMap is a handy TreeMap interface adding navi methods - higherKey, lowerKey
    private final NavigableMap<Integer, PendingMessage> buffer = new TreeMap<>();

    public RocketTracker(String channel) {
        this.status = new RocketStatus(channel);
        log.info("Created new tracker for channel {}", channel);
    }

    /**
     * Current rocket status snapshot
     */
    public RocketStatus snapshot() {
        return status;
    }

    /**
     * Put new message in buffer and apply if it is next by order
     */
    public void stageAndApply(int messageNumber, OffsetDateTime time, MessageType type, JsonNode payload) {
        lock.lock();
        try {
            if (messageNumber <= lastApplied) {
                log.warn("Ignored duplicate/old message: channel {}, number {}", status.getChannel(), messageNumber);
                return;
            }
            buffer.put(messageNumber, new PendingMessage(messageNumber, time, type, payload));
            log.debug("Buffered message: channel {}, number {}, type {}", status.getChannel(), messageNumber, type);

            applyPendingInOrder();
        } finally {
            lock.unlock();
        }
    }

    /**
     * applies current rocket state on a new message.
     */
    private void apply(PendingMessage pendingMessage) {
        if (status.isExploded() && pendingMessage.type != MessageType.RocketExploded) {
            log.warn("Ignored message for exploded rocket: channel {}, number {}, type {}",
                    status.getChannel(), pendingMessage.messageNumber(), pendingMessage.type());

            return;
        }

        switch (pendingMessage.type) {
            case RocketLaunched -> {
                status.setType(safeGetText(
                        pendingMessage.payload,
                        RocketMessageFields.TYPE,
                        status.getType()));

                status.setSpeed(safeGetInt(
                        pendingMessage.payload,
                        RocketMessageFields.LAUNCH_SPEED,
                        status.getSpeed()));

                status.setMission(safeGetText(
                        pendingMessage.payload,
                        RocketMessageFields.MISSION,
                        status.getMission()));

                status.setLastMessageTime(pendingMessage.time());

                log.info("Rocket launched: channel {}, type {}, mission {}, speed {}",
                        status.getChannel(), status.getType(), status.getMission(), status.getSpeed());

            }
            case RocketSpeedIncreased -> {
                int by = safeGetInt(
                        pendingMessage.payload,
                        RocketMessageFields.BY,
                        0);

                status.setSpeed(status.getSpeed() + by);
                status.setLastMessageTime(pendingMessage.time);

                log.debug("Speed increased: channel {}, by {}, newSpeed {}", status.getChannel(), by, status.getSpeed());

            }
            case RocketSpeedDecreased -> {
                int by = safeGetInt(
                        pendingMessage.payload,
                        RocketMessageFields.BY,
                        0);

                status.setSpeed(Math.max(0, status.getSpeed() - by));
                status.setLastMessageTime(pendingMessage.time);
                log.debug("Speed decreased: channel {}, by {}, newSpeed {}", status.getChannel(), by, status.getSpeed());

            }
            case RocketMissionChanged -> {
                status.setMission(safeGetText(
                        pendingMessage.payload,
                        RocketMessageFields.NEW_MISSION,
                        status.getMission()));

                status.setLastMessageTime(pendingMessage.time);
                log.info("Mission changed: channel {}, newMission {}", status.getChannel(), status.getMission());

            }
            case RocketExploded -> {
                status.setExploded(true);
                status.setLastMessageTime(pendingMessage.time);
                log.error("Rocket exploded: channel {}", status.getChannel());

            }
        }
    }

    /**
     * applies all messages that are in order in buffer. Stops if there is a gap in message numbers.
     */
    public void applyPendingInOrder() {
        while (true) {
            PendingMessage next = buffer.get(lastApplied + 1);
            if (next == null) break;
            apply(next);
            lastApplied = next.messageNumber;
            buffer.remove(next.messageNumber);
        }
    }


    /**
     * helpers to read JSON safely
     */
    private static String safeGetText(JsonNode node, String field, String def) {
        JsonNode value = node.get(field);
        return value != null && value.isTextual() ? value.asText() : def;
    }

    private static int safeGetInt(JsonNode node, String field, int def) {
        JsonNode value = node.get(field);
        return value != null && value.isNumber() ? value.asInt() : def;
    }

}
