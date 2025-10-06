package com.iot.device_connector.auth;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(
        @NotBlank(message = "username is required")
        String username,

        @NotBlank(message = "Password is required")
        String password) {
}
