package com.iot.command_control_service.controller;

import lombok.NonNull;
import java.util.UUID;

public record CommandRequest(
        @NonNull
        UUID deviceId,
        @NonNull
        UUID userId,
        @NonNull
        DeviceType deviceType,
        @NonNull
        String payload) {
}
