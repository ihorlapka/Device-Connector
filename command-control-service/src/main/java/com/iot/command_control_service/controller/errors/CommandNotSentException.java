package com.iot.command_control_service.controller.errors;

import java.util.UUID;

public class CommandNotSentException extends RuntimeException {

    public CommandNotSentException(UUID deviceId, UUID userId, Throwable t) {
        super("Unable to send command message for deviceId: " + deviceId + " for userId: " + userId, t);
    }
}
