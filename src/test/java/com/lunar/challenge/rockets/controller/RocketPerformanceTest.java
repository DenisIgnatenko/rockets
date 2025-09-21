package com.lunar.challenge.rockets.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lunar.challenge.rockets.dto.Metadata;
import com.lunar.challenge.rockets.dto.RocketMessage;
import com.lunar.challenge.rockets.domain.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RocketPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    @DisplayName("Should handle 50 concurrent updates correctly")
    void shouldHandleConcurrentMessages() throws Exception {
        int threads = 50;

        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            CountDownLatch latch = new CountDownLatch(threads);

            for(int i = 0; i < threads; i++) {
                int messageNumber = i + 1;
                executor.submit(() -> {
                    try {
                        var metadata = Metadata.builder()
                                .channel("concurrent-rocket")
                                .messageNumber(messageNumber)
                                .messageTime(OffsetDateTime.now())
                                .messageType(MessageType.RocketSpeedIncreased.name())
                                .build();

                        var message = new RocketMessage(metadata,
                                mapper.createObjectNode().put("by", 10));

                        mockMvc.perform(post("/messages")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(mapper.writeValueAsString(message)))
                                .andExpect(status().isOk());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            boolean completed = latch.await(10, TimeUnit.SECONDS);
            assertThat(completed).isTrue();
        }

        mockMvc.perform(get("/rockets/concurrent-rocket"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.speed").value(threads * 10));
    }

    @Test
    @DisplayName("Should measure latency between POST update and GET read")
    void shouldMeasureLatency() throws Exception {
        var metadata = Metadata.builder()
                .channel("latency-rocket")
                .messageNumber(1)
                .messageTime(OffsetDateTime.now())
                .messageType(MessageType.RocketLaunched.name())
                .build();

        var message = new RocketMessage(metadata,
                mapper.createObjectNode()
                        .put("type", "Falcon-9")
                        .put("mission", "Test")
                        .put("launchSpeed", 0));

        long start = System.nanoTime();

        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(message)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/rockets/latency-rocket"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("Falcon-9"));

        long end = System.nanoTime();
        long latencyMs = TimeUnit.NANOSECONDS.toMillis(end - start);

        System.out.println("Update → Read latency: " + latencyMs + " ms");

        assertThat(latencyMs).isLessThan(100);
    }
}