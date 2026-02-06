package com.iot.device_connector.devices.dto;

import com.iot.device_connector.devices.ThermostatMode;
import com.iot.device_connector.model.enums.DeviceStatus;
import com.iot.device_connector.model.enums.DeviceType;

import java.time.Instant;
import java.util.UUID;

public record ThermostatDto(
        UUID deviceId,
        Float currentTemperature,
        Float targetTemperature,
        Float humidity,
        ThermostatMode mode,
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

    public Float getCurrentTemperature() {
        return currentTemperature;
    }

    public Float getTargetTemperature() {
        return targetTemperature;
    }

    public Float getHumidity() {
        return humidity;
    }

    @Override
    public DeviceType getDeviceType() {
        return deviceType;
    }
}
