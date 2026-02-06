package com.iot.device_connector.devices.dto;

import com.iot.device_connector.model.enums.DeviceStatus;
import com.iot.device_connector.model.enums.DeviceType;

import java.time.Instant;
import java.util.UUID;

public record SmartLightDto(
        UUID deviceId,
        Boolean isOn,
        Integer brightness,
        String colour,
        String mode,
        Float powerConsumption,
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

    @Override
    public DeviceType getDeviceType() {
        return deviceType;
    }

    public Integer getBrightness() {
        return brightness;
    }

    public Float getPowerConsumption() {
        return powerConsumption;
    }
}
