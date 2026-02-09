package com.iot.command_control_service.controller.dto;

import java.time.Instant;

public interface CommandDto {
    String getCommandId();
    String getDeviceId();
    Instant getCreatedAt();
}
