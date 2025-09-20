package com.lunar.challenge.rockets.repository;

import com.lunar.challenge.rockets.domain.RocketTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
@Profile("inmemory")
public class InMemoryRocketRepository implements RocketRepository {
    private static final Logger log = LoggerFactory.getLogger(InMemoryRocketRepository.class);

    private final Map<String, RocketTracker> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<RocketTracker> findByChannel(String channel) {
        log.debug("Fetching rocket by channel {}", channel);
        return Optional.ofNullable(storage.get(channel));
    }

    @Override
    public List<RocketTracker> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void save(RocketTracker tracker) {
        log.debug("Saving rocket tracker log for channel {}", tracker.getStatus().getChannel());
        storage.put(tracker.getStatus().getChannel(), tracker);
    }
}
