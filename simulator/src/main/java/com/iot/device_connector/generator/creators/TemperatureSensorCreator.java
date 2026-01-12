package com.iot.device_connector.generator.creators;

import com.iot.device_connector.model.Device;
import com.iot.devices.TempUnit;
import com.iot.devices.TemperatureSensor;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static com.iot.device_connector.generator.Utils.updateFirmwareVersion;
import static com.iot.device_connector.generator.Utils.updateStatus;
import static com.iot.devices.DeviceStatus.ONLINE;
import static java.time.Instant.now;

@Component
@RequiredArgsConstructor
public class TemperatureSensorCreator {

    private final Random random = new Random();
    private final ConcurrentHashMap<String, TemperatureSensor> telemetriesById = new ConcurrentHashMap<>();

    public SpecificRecord create(Device device) {
        if (!telemetriesById.containsKey(device.id().toString())) {
            final TemperatureSensor temperatureSensor = TemperatureSensor.newBuilder()
                    .setDeviceId(device.id().toString())
                    .setTemperature(22f)
                    .setHumidity(45f)
                    .setPressure(0.5f)
                    .setUnit(TempUnit.C)
                    .setStatus(ONLINE)
                    .setFirmwareVersion("2.1")
                    .setLastUpdated(now())
                    .build();
            telemetriesById.put(temperatureSensor.getDeviceId(), temperatureSensor);
            return temperatureSensor;
        } else {
            return telemetriesById.computeIfPresent(device.id().toString(), (id, temperatureSensor) -> updateTelemetries(temperatureSensor));
        }
    }

    private TemperatureSensor updateTelemetries(TemperatureSensor ts) {
        final int index = random.nextInt(5);
        switch (index) {
            case 0 -> ts.setTemperature(random.nextBoolean() ? ts.getTemperature() + 0.5f : ts.getTemperature() - 0.5f);
            case 1 -> ts.setHumidity(random.nextBoolean() ? ts.getHumidity() + 0.5f : ts.getHumidity() - 0.1f);
            case 2 -> ts.setPressure(random.nextBoolean() ? ts.getPressure() + 0.1f : ts.getPressure() - 0.1f);
            case 3 -> ts.setStatus(updateStatus(random));
            case 4 -> ts.setFirmwareVersion(updateFirmwareVersion(ts.getFirmwareVersion()));
        }
        ts.setLastUpdated(now());
        return ts;
    }
}
