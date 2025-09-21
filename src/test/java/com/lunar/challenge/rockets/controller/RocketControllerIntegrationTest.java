package com.lunar.challenge.rockets.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lunar.challenge.rockets.domain.MessageType;
import com.lunar.challenge.rockets.dto.Metadata;
import com.lunar.challenge.rockets.dto.RocketMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RocketControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should accept messages and return rocket state via /rockets")
    void shouldProcessMessagesAndReturnRocketState() throws Exception {
        // given
        Metadata metadata = Metadata.builder()
                .channel("integration-1")
                .messageNumber(1)
                .messageTime(OffsetDateTime.now())
                .messageType(MessageType.RocketLaunched.name())
                .build();

        String payloadJson = """
                {
                  "type": "Falcon-9",
                  "mission": "STARLINK",
                  "launchSpeed": 1000
                }
                """;

        RocketMessage message = new RocketMessage(metadata,
                objectMapper.readTree(payloadJson));

        // when: отправляем POST /messages
        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk());

        // then: GET /rockets вернет список с этой ракетой
        String response = mockMvc.perform(get("/rockets"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).contains("Falcon-9").contains("STARLINK");
    }

    @Test
    @DisplayName("Should return rockets sorted by channel")
    void shouldReturnRocketsSortedByChannel() throws Exception {
        // given — две ракеты с разными channel
        for(int i = 0; i < 2; i++) {
            Metadata metadata = Metadata.builder()
                    .channel("ch-" + (2 - i)) // ch-2, ch-1
                    .messageNumber(1)
                    .messageTime(OffsetDateTime.now())
                    .messageType(MessageType.RocketLaunched.name())
                    .build();

            String payloadJson = """
                    {
                      "type": "TestRocket",
                      "mission": "TEST",
                      "launchSpeed": 10
                    }
                    """;

            RocketMessage message = new RocketMessage(metadata,
                    objectMapper.readTree(payloadJson));

            mockMvc.perform(post("/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(message)))
                    .andExpect(status().isOk());
        }

        // when
        String response = mockMvc.perform(get("/rockets"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // then — сортировка по channel
        int idx1 = response.indexOf("ch-1");
        int idx2 = response.indexOf("ch-2");
        assertThat(idx1).isLessThan(idx2);
    }
}