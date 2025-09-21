package com.lunar.challenge.rockets.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.lunar.challenge.rockets.domain.MessageType;
import com.lunar.challenge.rockets.domain.RocketTracker;
import com.lunar.challenge.rockets.domain.event.RocketEvent;
import com.lunar.challenge.rockets.dto.Metadata;
import com.lunar.challenge.rockets.dto.RocketStatusView;
import com.lunar.challenge.rockets.exception.RocketNotFoundException;
import com.lunar.challenge.rockets.repository.RocketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RocketService {
    private final RocketRepository repository;

    /**
     * Apply incoming message to rocket state
     */
    public void handleMessage(Metadata metadata, JsonNode payload) {
        String channel = metadata.getChannel();
        int messageNumber = metadata.getMessageNumber();
        OffsetDateTime messageTime = metadata.getMessageTime();
        MessageType messageType = MessageType.valueOf(metadata.getMessageType());

        // JSON -> Rocket Event
        RocketEvent event = EventMapper.from(messageType, payload);

        // creating tracker
        RocketTracker tracker = repository.findByChannel(channel)
                .orElseGet(() -> {
                    log.info("Creating new tracker for channel {}", channel);
                    return new RocketTracker(channel);
                });
        
        // applying events
        tracker.stageAndApply(messageNumber, messageTime, messageType, event);
        repository.save(tracker);

        log.debug("Applied message {} type {} to channel {}",
                messageNumber, event.getClass().getSimpleName(), channel);
    }


    /**
     * Get current status of a given single rocket
     *
     * @param channel of rocket
     * @return current rocket view snapshot
     * @throws RocketNotFoundException if rocket not found
     */
    public RocketStatusView getRocket(String channel) {
        RocketTracker tracker = repository.findByChannel(channel)
                .orElseThrow(() -> new RocketNotFoundException(channel));

        return toView(tracker);
    }

    /**
     * Get list of all rockets sorted by channel
     */
    public List<RocketStatusView> getAllRockets() {
        return repository.findAll().stream()
                .map(this::toView)
                .sorted(Comparator.comparing(RocketStatusView::getChannel))
                .toList();
    }

    private RocketStatusView toView(RocketTracker tracker) {
        var status = tracker.snapshot();
        log.info("Building view for channel {}, status: type={}, mission={}, speed={}, time={}",
                status.getChannel(), status.getType(), status.getMission(),
                status.getSpeed(), status.getLastMessageTime());

        return RocketStatusView.builder()
                .channel(status.getChannel())
                .type(status.getType())
                .speed(status.getSpeed())
                .mission(status.getMission())
                .exploded(status.isExploded())
                .lastMessageTime(status.getLastMessageTime())
                .history(tracker.getHistory())
                .build();
    }
}
