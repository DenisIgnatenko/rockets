package com.lunar.challenge.rockets.domain;

import com.lunar.challenge.rockets.domain.event.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RocketTrackerTest {
    private RocketTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new RocketTracker("channel-123");
    }

    @Test
    @DisplayName("Should apply RocketLaunched event and set type, mission, and initial speed")
    void shouldHandleRocketLaunched() {
        tracker.stageAndApply(1, OffsetDateTime.now(), MessageType.RocketLaunched,
                new RocketLaunched("Falcon-9", "STARLINK", 0));

        assertThat(tracker.getStatus().getType()).isEqualTo("Falcon-9");
        assertThat(tracker.getStatus().getMission()).isEqualTo("STARLINK");
        assertThat(tracker.getStatus().getSpeed()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should increase rocket speed when RocketSpeedIncreased event is applied")
    void shouldIncreaseSpeed() {
        tracker.stageAndApply(1, OffsetDateTime.now(), MessageType.RocketLaunched,
                new RocketLaunched("Falcon-9", "STARLINK", 0));
        tracker.stageAndApply(2, OffsetDateTime.now(), MessageType.RocketSpeedIncreased,
                new RocketSpeedIncreased(5000));

        assertThat(tracker.getStatus().getSpeed()).isEqualTo(5000);
    }

    @Test
    @DisplayName("Should decrease rocket speed when RocketSpeedDecreased event is applied")
    void shouldDecreaseSpeed() {
        tracker.stageAndApply(1, OffsetDateTime.now(), MessageType.RocketLaunched,
                new RocketLaunched("Falcon-9", "STARLINK", 0));
        tracker.stageAndApply(2, OffsetDateTime.now(), MessageType.RocketSpeedIncreased,
                new RocketSpeedIncreased(5000));
        tracker.stageAndApply(3, OffsetDateTime.now(), MessageType.RocketSpeedDecreased,
                new RocketSpeedDecreased(2000));

        assertThat(tracker.getStatus().getSpeed()).isEqualTo(3000);
    }

    @Test
    @DisplayName("Should change mission when RocketMissionChanged event is applied")
    void shouldChangeMission() {
        tracker.stageAndApply(1, OffsetDateTime.now(), MessageType.RocketLaunched,
                new RocketLaunched("Falcon-9", "STARLINK", 0));
        tracker.stageAndApply(2, OffsetDateTime.now(), MessageType.RocketMissionChanged,
                new RocketMissionChanged("MARS"));

        assertThat(tracker.getStatus().getMission()).isEqualTo("MARS");
    }

    @Test
    @DisplayName("Should mark rocket as exploded when RocketExploded event is applied")
    void shouldMarkAsExploded() {
        tracker.stageAndApply(1, OffsetDateTime.now(), MessageType.RocketLaunched,
                new RocketLaunched("Falcon-9", "STARLINK", 0));
        tracker.stageAndApply(2, OffsetDateTime.now(), MessageType.RocketExploded,
                new RocketExploded());

        assertThat(tracker.getStatus().isExploded()).isTrue();
    }

    @Test
    @DisplayName("Should ignore duplicate messages with the same messageNumber")
    void shouldIgnoreDuplicateMessageNumber() {
        tracker.stageAndApply(1, OffsetDateTime.now(), MessageType.RocketLaunched,
                new RocketLaunched("Falcon-9", "STARLINK", 0));
        tracker.stageAndApply(2, OffsetDateTime.now(), MessageType.RocketSpeedIncreased,
                new RocketSpeedIncreased(5000));
        tracker.stageAndApply(2, OffsetDateTime.now(), MessageType.RocketSpeedIncreased,
                new RocketSpeedIncreased(7000)); // duplicate

        assertThat(tracker.getStatus().getSpeed()).isEqualTo(5000);
    }

    @Test
    @DisplayName("Should ignore older messages with lower messageNumber")
    void shouldIgnoreOlderMessages() {
        tracker.stageAndApply(1, OffsetDateTime.now(), MessageType.RocketLaunched,
                new RocketLaunched("Falcon-9", "STARLINK", 0));
        tracker.stageAndApply(2, OffsetDateTime.now(), MessageType.RocketSpeedIncreased,
                new RocketSpeedIncreased(5000));
        tracker.stageAndApply(1, OffsetDateTime.now(), MessageType.RocketSpeedIncreased,
                new RocketSpeedIncreased(10000)); //old message

        assertThat(tracker.getStatus().getSpeed()).isEqualTo(5000);
    }
}