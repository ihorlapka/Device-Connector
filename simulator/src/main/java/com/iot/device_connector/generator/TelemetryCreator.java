package com.iot.device_connector.generator;

import com.iot.device_connector.generator.creators.*;
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
    private final SmartPlugCreator smartPlugCreator;
    private final SoilMoistureSensorCreator soilMoistureSensorCreator;
    private final TemperatureSensorCreator temperatureSensorCreator;
    private final ThermostatCreator thermostatCreator;

    public SpecificRecord create(Device device) {
        return switch (device.deviceType()) {
            case DOOR_SENSOR -> doorSensorCreator.create(device);
            case ENERGY_METER -> energyMeterCreator.create(device);
            case SMART_LIGHT -> smartLightCreator.create(device);
            case SMART_PLUG -> smartPlugCreator.create(device);
            case SOIL_MOISTURE_SENSOR -> soilMoistureSensorCreator.create(device);
            case TEMPERATURE_SENSOR -> temperatureSensorCreator.create(device);
            case THERMOSTAT -> thermostatCreator.create(device);
            default -> throw new IllegalArgumentException("Unknown device type detected");
        };
    }
}
