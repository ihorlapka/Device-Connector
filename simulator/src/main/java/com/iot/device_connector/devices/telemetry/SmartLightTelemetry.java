package com.iot.device_connector.devices.telemetry;

import lombok.Value;

@Value
public class SmartLightTelemetry {
    Boolean isOn;
    Integer brightness;
    String colour;
    String mode;
    Float powerConsumption;
}
