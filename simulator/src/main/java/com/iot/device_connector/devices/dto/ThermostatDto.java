package com.iot.device_connector.devices.dto;

import com.iot.device_connector.devices.ThermostatMode;
import com.iot.device_connector.model.enums.DeviceStatus;
import com.iot.device_connector.model.enums.DeviceType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor(staticName = "of")
public class ThermostatDto implements DeviceDto {
    private final UUID deviceId;
    private final DeviceStatus status;
    private final String firmwareVersion;
    private final Instant lastUpdated;
    private final DeviceType deviceType;
    private Float currentTemperature;
    private Float targetTemperature;
    private Float humidity;
    private ThermostatMode mode;
}
