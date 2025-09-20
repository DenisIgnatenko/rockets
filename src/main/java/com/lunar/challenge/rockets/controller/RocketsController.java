package com.lunar.challenge.rockets.controller;

import com.lunar.challenge.rockets.dto.ErrorResponse;
import com.lunar.challenge.rockets.dto.RocketStatusView;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/rockets")
@RequiredArgsConstructor
@Tag(name = "Rockets", description = "Query current rocket states")
public class RocketsController {
    private static final Logger log = LoggerFactory.getLogger(RocketsController.class);

    private final RocketService service;

    @GetMapping("/{channel}")
    @Operation(
            summary = "Get rocket by channel",
            description = "Returns the current state of a specific rocket identified by its channel."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rocket state returned"),
            @ApiResponse(responseCode = "404", description = "Rocket not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RocketStatusView> getRocket(@PathVariable String channel) {
        return ResponseEntity.ok(service.getRocket(channel));
    }

    @GetMapping
    @Operation(
            summary = "List all rockets",
            description = "Returns the current states of all rockets, sorted by channel."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of rockets returned"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<RocketStatusView>> getAllRockets() {
        return ResponseEntity.ok(service.getAllRockets());
    }

}
