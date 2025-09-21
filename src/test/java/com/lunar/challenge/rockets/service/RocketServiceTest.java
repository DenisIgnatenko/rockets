package com.lunar.challenge.rockets.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lunar.challenge.rockets.domain.MessageType;
import com.lunar.challenge.rockets.domain.RocketTracker;
import com.lunar.challenge.rockets.dto.Metadata;
import com.lunar.challenge.rockets.dto.RocketStatusView;
import com.lunar.challenge.rockets.exception.RocketNotFoundException;
import com.lunar.challenge.rockets.repository.RocketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class RocketServiceTest {

    private RocketRepository repository;
    private RocketService service;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        repository = mock(RocketRepository.class);
        service = new RocketService(repository);
        mapper = new ObjectMapper();
    }

    @Test
    void shouldCreateNewTrackerOnFirstMessage() {
        Metadata metadata = Metadata.builder()
                .channel("channel-1")
                .messageNumber(1)
                .messageTime(OffsetDateTime.now())
                .messageType(MessageType.RocketLaunched.name())
                .build();

        ObjectNode payload = mapper.createObjectNode()
                .put("type", "Falcon-9")
                .put("mission", "STARLINK")
                .put("launchSpeed", 1000);

        when(repository.findByChannel("channel-1")).thenReturn(Optional.empty());

        service.handleMessage(metadata, payload);

        verify(repository).save(any(RocketTracker.class));
    }

    @Test
    void shouldThrowIfRocketNotFound() {
        when(repository.findByChannel("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRocket("unknown"))
                .isInstanceOf(RocketNotFoundException.class)
                .hasMessageContaining("unknown");
    }

    @Test
    void shouldMapStatusToView() {
        RocketTracker tracker = new RocketTracker("channel-2");
        tracker.stageAndApply(
                1,
                OffsetDateTime.now(),
                MessageType.RocketLaunched,
                new com.lunar.challenge.rockets.domain.event.RocketLaunched("Falcon-9", "STARLINK", 1000)
        );

        when(repository.findByChannel("channel-2")).thenReturn(Optional.of(tracker));

        RocketStatusView view = service.getRocket("channel-2");

        assertThat(view.getChannel()).isEqualTo("channel-2");
        assertThat(view.getType()).isEqualTo("Falcon-9");
        assertThat(view.getMission()).isEqualTo("STARLINK");
        assertThat(view.getSpeed()).isEqualTo(1000);
        assertThat(view.isExploded()).isFalse();
    }
}