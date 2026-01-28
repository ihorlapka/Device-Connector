package com.iot.command_control_service.controller;

import lombok.NonNull;

import java.time.Instant;

public record CommandDto(
        @NonNull
        String commandId,
        @NonNull
        String deviceId,
        @NonNull
        String userId,
        @NonNull
        String payload,
        @NonNull
        Instant createdAt
) {
}
