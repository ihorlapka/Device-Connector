package com.iot.device_connector.generator;

import com.iot.device_connector.rest.Device;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TelemetryCreator {

    private final DoorSensorCreator doorSensorCreator;
    private final EnergyMeterCreator energyMeterCreator;

    public SpecificRecord create(Device device) {
        return switch (device.deviceType()) {
            case DOOR_SENSOR -> doorSensorCreator.create(device);
            case ENERGY_METER -> energyMeterCreator.create(device);
            default -> throw new IllegalArgumentException("Unknown device type detected");
        };
    }

    private SpecificRecord createEnergyMeterTelemetry(Device device) {
        return null;
    }
}
