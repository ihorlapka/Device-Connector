package com.iot.device_connector.rest;

import java.util.Set;
import java.util.UUID;

public record User(UUID id,
                   String username,
                   String firstName,
                   String lastName,
                   String phone,
                   String email,
                   String address,
                   Set<Device> devices) {
}
