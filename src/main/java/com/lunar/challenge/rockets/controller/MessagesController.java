package com.lunar.challenge.rockets.controller;

import com.lunar.challenge.rockets.dto.ErrorResponse;
import com.lunar.challenge.rockets.dto.RocketMessage;
import com.lunar.challenge.rockets.service.RocketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Receiving rocket state messages")
public class MessagesController {
    private static final Logger log = LoggerFactory.getLogger(MessagesController.class);

    private final RocketService service;

    @PostMapping
    @Operation(
            summary = "Receive new rocket message",
            description = "Consumes incoming rocket telemetry (metadata + payload) and updates rocket state."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Message processed successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> receiveMessage(@RequestBody RocketMessage message) {
        log.debug("Received message for channel {}, type {}",
                message.getMetadata().getChannel(),
                message.getMetadata());

        service.handleMessage(message.getMetadata(), message.getMessage());

        return ResponseEntity.ok().build();
    }
}
