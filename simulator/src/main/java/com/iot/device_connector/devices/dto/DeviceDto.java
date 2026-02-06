package com.iot.device_connector.devices.dto;

import com.iot.device_connector.model.enums.DeviceStatus;
import com.iot.device_connector.model.enums.DeviceType;

import java.time.Instant;
import java.util.UUID;

public interface DeviceDto {
    UUID getDeviceId();
    Instant getLastUpdated();
    DeviceStatus getStatus();
    DeviceType getDeviceType();
}
