package com.iot.device_connector.generator;

import com.iot.device_connector.model.Device;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TelemetryCreator {

    private final DoorSensorCreator doorSensorCreator;
    private final EnergyMeterCreator energyMeterCreator;
    private final SmartLightCreator smartLightCreator;

    public SpecificRecord create(Device device) {
        return switch (device.deviceType()) {
            case DOOR_SENSOR -> doorSensorCreator.create(device);
            case ENERGY_METER -> energyMeterCreator.create(device);
            case SMART_LIGHT -> smartLightCreator.create(device);
            default -> throw new IllegalArgumentException("Unknown device type detected");
        };
    }
}
