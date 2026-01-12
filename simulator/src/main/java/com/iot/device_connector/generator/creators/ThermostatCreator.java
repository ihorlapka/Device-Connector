package com.iot.device_connector.generator.creators;

import com.iot.device_connector.model.Device;
import com.iot.devices.Thermostat;
import com.iot.devices.ThermostatMode;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static com.iot.device_connector.generator.Utils.updateFirmwareVersion;
import static com.iot.device_connector.generator.Utils.updateStatus;
import static com.iot.devices.DeviceStatus.ONLINE;
import static com.iot.devices.ThermostatMode.*;
import static java.time.Instant.now;

@Component
@RequiredArgsConstructor
public class ThermostatCreator {

    private final Random random = new Random();
    private final ConcurrentHashMap<String, Thermostat> telemetriesById = new ConcurrentHashMap<>();

    public SpecificRecord create(Device device) {
        if (!telemetriesById.containsKey(device.id().toString())) {
            final Thermostat thermostat = Thermostat.newBuilder()
                    .setDeviceId(device.id().toString())
                    .setCurrentTemperature(22f)
                    .setTargetTemperature(25f)
                    .setHumidity(45f)
                    .setMode(HEAT)
                    .setStatus(ONLINE)
                    .setFirmwareVersion("2.1")
                    .setLastUpdated(now())
                    .build();
            telemetriesById.put(thermostat.getDeviceId(), thermostat);
            return thermostat;
        } else {
            return telemetriesById.computeIfPresent(device.id().toString(), (id, thermostat) -> updateTelemetries(thermostat));
        }
    }

    private Thermostat updateTelemetries(Thermostat t) {
        final int index = random.nextInt(6);
        switch (index) {
            case 0 -> t.setCurrentTemperature(random.nextBoolean() ? t.getCurrentTemperature() + 0.2f : t.getCurrentTemperature() - 0.2f);
            case 1 -> {
                switch (t.getMode()) {
                    case HEAT -> t.setTargetTemperature(t.getTargetTemperature() + 0.5f);
                    case COOL -> t.setTargetTemperature(t.getTargetTemperature() - 0.5f);
                    case AUTO -> t.setTargetTemperature(random.nextBoolean() ? t.getTargetTemperature() + 0.5f : t.getTargetTemperature() - 0.5f);
                    case OFF -> t.setTargetTemperature(t.getCurrentTemperature());
                }
            }
            case 2 -> t.setHumidity(random.nextBoolean() ? t.getHumidity() + 0.1f : t.getHumidity() - 0.1f);
            case 3 -> {
                int modeIndex = random.nextInt(4);
                ThermostatMode mode = UNKNOWN;
                switch (modeIndex) {
                    case 0 -> mode = HEAT;
                    case 1 -> mode = COOL;
                    case 2 -> mode = AUTO;
                    case 3 -> mode = OFF;
                }
                t.setMode(mode);
            }
            case 4 -> t.setStatus(updateStatus(random));
            case 5 -> t.setFirmwareVersion(updateFirmwareVersion(t.getFirmwareVersion()));
        }
        t.setLastUpdated(now());
        return t;
    }
}
