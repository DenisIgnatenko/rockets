package com.lunar.challenge.rockets.domain;

import com.lunar.challenge.rockets.domain.event.*;
import com.lunar.challenge.rockets.dto.RocketHistoryItem;
import com.lunar.challenge.rockets.dto.RocketSnapshotDTO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private final List<AppliedEvent> appliedEvents = new ArrayList<>();

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
    private record PendingEvent(int messageNumber,
                                OffsetDateTime time,
                                RocketEvent event
    ) {
    }


    //we need the structure that can store entries sorted by the messageNubmer (key)
    //in hashmap keys are unsorted, harder to search for next
    //arraylist if we receive messageNumber 1000, we will have to create list with 1001 elements (999 empty)
    //- uneffective by memory and time.
    //Treemap - good choise.
    //NavigableMap is a handy TreeMap interface adding navi methods - higherKey, lowerKey
    private final NavigableMap<Integer, PendingEvent> buffer = new TreeMap<>();

    public RocketTracker(String channel) {
        this.status = new RocketStatus(channel);
        log.info("Created new tracker for channel {}", channel);
    }

    public RocketStatus snapshot() {
        return status;
    }

    public List<AppliedEvent> getHistory() {
        return List.copyOf(appliedEvents);
    }

    /**
     * Put new message in buffer and apply if it is next by order
     */
    public void stageAndApply(int messageNumber, OffsetDateTime time, MessageType type, RocketEvent event) {
        lock.lock();
        try {
            if (messageNumber <= lastApplied) {
                log.warn("Ignored duplicate/old message: channel {}, number {}", status.getChannel(), messageNumber);
                return;
            }
            buffer.put(messageNumber, new PendingEvent(messageNumber, time, event));
            log.debug("Buffered message: channel {}, number {}, type {}", status.getChannel(), messageNumber, type);
            applyPendingInOrder();
        } finally {
            lock.unlock();
        }
    }

    /**
     * applies current rocket state on a new message.
     */
    private void apply(PendingEvent pending) {
        if (status.isExploded() && !(pending.event instanceof RocketExploded)) {
            log.warn("Ignored event for exploded rocket: channel {}, number {}, type {}",
                    status.getChannel(), pending.messageNumber, pending.event().getClass().getSimpleName());
            return;
        }

        RocketEvent event = pending.event();
        if (event instanceof RocketLaunched(String type, String mission, int initialSpeed)) {
            status.setType(type);
            status.setMission(mission);
            status.setSpeed(initialSpeed);
            status.setLastMessageTime(pending.time());
            log.info("Rocket launched: channel {}, type {}, mission {}, speed {}",
                    status.getChannel(), type, mission, initialSpeed);
        } else if (event instanceof RocketMissionChanged(String newMission)) {
            status.setMission(newMission);
            status.setLastMessageTime(pending.time());
            log.info("Mission changed: channel {}, newMission {}", status.getChannel(), newMission);
        } else if (event instanceof RocketExploded) {
            status.setExploded(true);
            status.setLastMessageTime(pending.time());
            log.error("Rocket exploded: channel {}", status.getChannel());
        } else if (event instanceof RocketSpeedIncreased(int delta)) {
            status.setSpeed(status.getSpeed() + delta);
            status.setLastMessageTime(pending.time());
            log.debug("Speed increased: channel {}, by {}, newSpeed {}",
                    status.getChannel(), delta, status.getSpeed());
        } else if (event instanceof RocketSpeedDecreased(int delta)) {
            status.setSpeed(status.getSpeed() - delta);
            status.setLastMessageTime(pending.time());
            log.debug("Speed decreased: channel {}, by {}, newSpeed {}",
                    status.getChannel(), delta, status.getSpeed());
        }

        status.setLastMessageTime(pending.time());
        appliedEvents.add(new AppliedEvent(
                pending.time,
                MessageType.valueOf(event.getClass().getSimpleName()), // фикс
                status.getSpeed()
        ));
    }

    /**
     * applies all messages that are in order in buffer. Stops if there is a gap in message numbers.
     */
    private void applyPendingInOrder() {
        while (true) {
            PendingEvent next = buffer.get(lastApplied + 1);
            if (next == null) break;
            apply(next);
            lastApplied = next.messageNumber();
            buffer.remove(next.messageNumber());
        }
    }

    public RocketSnapshotDTO toSnapshot() {
        return RocketSnapshotDTO.builder()
                .channel(status.getChannel())
                .type(status.getType())
                .mission(status.getMission())
                .speed(status.getSpeed())
                .exploded(status.isExploded())
                .lastMessageTime(status.getLastMessageTime())
                .history(appliedEvents.stream()
                        .map(event -> RocketHistoryItem.builder()
                                .time(event.time())
                                .type(event.type().name())
                                .speedAfter(event.speedAfter())
                                .build())
                        .toList())
                .build();
    }

    public void restoreFromSnapshot(RocketSnapshotDTO dto) {
        status.setType(dto.getType());
        status.setMission(dto.getMission());
        status.setSpeed(dto.getSpeed());
        status.setExploded(dto.isExploded());
        status.setLastMessageTime(dto.getLastMessageTime());

        appliedEvents.clear();
        if (dto.getHistory() != null) {
            dto.getHistory().forEach(history -> appliedEvents.add(
                    new AppliedEvent(
                            history.getTime(),
                            MessageType.valueOf(history.getType()),
                            history.getSpeedAfter()
                    )
            ));
            lastApplied = dto.getHistory().size();
        } else {
            lastApplied = 0;
        }
    }
}
