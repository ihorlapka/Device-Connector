package com.iot.device_connector.devices.dto;

import com.iot.device_connector.model.enums.DeviceStatus;
import com.iot.device_connector.model.enums.DeviceType;

import java.time.Instant;
import java.util.UUID;

public record SmartPlugDto(UUID deviceId,
                           Boolean isOn,
                           Float voltage,
                           Float current,
                           Float powerUsage,
                           DeviceStatus status,
                           String firmwareVersion,
                           Instant lastUpdated,
                           DeviceType deviceType) implements DeviceDto {

    @Override
    public UUID getDeviceId() {
        return deviceId;
    }

    @Override
    public Instant getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public DeviceStatus getStatus() {
        return status;
    }

    public Float getVoltage() {
        return voltage;
    }

    public Float getCurrent() {
        return current;
    }

    public Float getPowerUsage() {
        return powerUsage;
    }

    @Override
    public DeviceType getDeviceType() {
        return deviceType;
    }
}
