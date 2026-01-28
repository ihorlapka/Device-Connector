package com.iot.command_control_service.controller.errors;

import java.util.UUID;

public class PermissionDeniedException extends RuntimeException {

    public PermissionDeniedException(UUID userId) {
        super("Permission denied for userId: " + userId);
    }
}
