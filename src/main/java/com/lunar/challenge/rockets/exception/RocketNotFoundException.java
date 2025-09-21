package com.lunar.challenge.rockets.exception;

/**
 * Thrown when a rocket with given channel is not found.
 */
public class RocketNotFoundException extends RuntimeException {
    public RocketNotFoundException(String channel) {
        super("Rocket not found: " + channel);
    }
}