package com.lunar.challenge.rockets.repository;

import com.lunar.challenge.rockets.domain.RocketTracker;

import java.util.List;
import java.util.Optional;

public interface RocketRepository {
    Optional<RocketTracker> findByChannel(String channel);

    List<RocketTracker> findAll();

    void save(RocketTracker tracker);
}
