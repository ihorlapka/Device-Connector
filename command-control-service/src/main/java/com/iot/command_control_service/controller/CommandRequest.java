package com.iot.command_control_service.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

import java.util.UUID;

public record CommandRequest(
        @NonNull
        UUID deviceId,
        @NonNull
        UUID userId,
        @NotBlank(message = "Payload mast not be blank")
        String payload) {
}
