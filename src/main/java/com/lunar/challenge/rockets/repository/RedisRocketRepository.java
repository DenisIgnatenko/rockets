package com.lunar.challenge.rockets.repository;

import com.lunar.challenge.rockets.domain.RocketTracker;
import com.lunar.challenge.rockets.dto.RocketSnapshotDTO;
import com.lunar.challenge.rockets.repository.RocketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Profile("redis")
public class RedisRocketRepository implements RocketRepository {
    private final RedisTemplate<String, RocketSnapshotDTO> redisTemplate;
    private static final String PREFIX = "rocket:";
    private static final Duration TTL = Duration.ofMinutes(5);

    @Override
    public Optional<RocketTracker> findByChannel(String channel) {
        RocketSnapshotDTO dto = redisTemplate.opsForValue().get(PREFIX + channel);
        if (dto == null) {
            return Optional.empty();
        }
        RocketTracker tracker = new RocketTracker(channel);
        tracker.restoreFromSnapshot(dto);
        return Optional.of(tracker);
    }

    @Override
    public void save(RocketTracker tracker) {
        redisTemplate.opsForValue().set(
                PREFIX + tracker.getChannel(),
                tracker.toSnapshot(),
                TTL
        );
    }

    @Override
    public List<RocketTracker> findAll() {
        return redisTemplate.keys(PREFIX + "*").stream()
                .map(key -> redisTemplate.opsForValue().get(key))
                .filter(Objects::nonNull)
                .map(snapshot -> {
                    RocketTracker tracker = new RocketTracker(snapshot.getChannel());
                    tracker.restoreFromSnapshot(snapshot);
                    return tracker;
                })
                .collect(Collectors.toList());
    }
}