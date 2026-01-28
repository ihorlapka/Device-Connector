package com.iot.command_control_service.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

import java.time.Instant;

public record CommandDto(
        @NotBlank(message = "commandId mast not be blank")
        String commandId,
        @NotBlank(message = "deviceId mast not be blank")
        String deviceId,
        @NotBlank(message = "userId mast not be blank")
        String userId,
        @NotBlank(message = "Payload mast not be blank")
        String payload,
        @NonNull
        Instant createdAt
) {
}
