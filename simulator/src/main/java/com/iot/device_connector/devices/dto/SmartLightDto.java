package com.iot.device_connector.devices.dto;

import com.iot.device_connector.model.enums.DeviceStatus;
import com.iot.device_connector.model.enums.DeviceType;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor
public class SmartLightDto implements DeviceDto {
    private final UUID deviceId;
    private final DeviceStatus status;
    private final String firmwareVersion;
    private final Instant lastUpdated;
    private final DeviceType deviceType;
    private Boolean isOn;
    private Integer brightness;
    private String colour;
    private String mode;
    private Float powerConsumption;
}
