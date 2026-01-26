package com.iot.device_connector.generator;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

public record GenerateTelemetryRequest(
        @NotBlank(message = "username is required")
        String username,
        int rpm,
        List<UUID> deviceIds) {
}
