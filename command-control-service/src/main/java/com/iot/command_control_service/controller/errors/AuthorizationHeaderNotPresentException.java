package com.iot.command_control_service.controller.errors;

public class AuthorizationHeaderNotPresentException extends RuntimeException {

    public AuthorizationHeaderNotPresentException() {
        super("Authorization header missed!");
    }
}
