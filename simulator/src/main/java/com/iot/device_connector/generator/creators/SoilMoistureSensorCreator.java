package com.iot.device_connector.generator.creators;

import com.iot.device_connector.model.Device;
import com.iot.devices.SoilMoistureSensor;
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
public class SoilMoistureSensorCreator {

    private final Random random = new Random();
    private final ConcurrentHashMap<String, SoilMoistureSensor> telemetriesById = new ConcurrentHashMap<>();

    public SpecificRecord create(Device device) {
        if (!telemetriesById.containsKey(device.id().toString())) {
            final SoilMoistureSensor soilMoistureSensor = SoilMoistureSensor.newBuilder()
                    .setDeviceId(device.id().toString())
                    .setMoisturePercentage(3f)
                    .setSoilTemperature(8f)
                    .setBatteryLevel(100)
                    .setStatus(ONLINE)
                    .setFirmwareVersion("2.1")
                    .setLastUpdated(now())
                    .build();
            telemetriesById.put(soilMoistureSensor.getDeviceId(), soilMoistureSensor);
            return soilMoistureSensor;
        } else {
            return telemetriesById.computeIfPresent(device.id().toString(), (id, soilMoistureSensor) -> updateTelemetries(soilMoistureSensor));
        }
    }

    private SoilMoistureSensor updateTelemetries(SoilMoistureSensor sms) {
        final int index = random.nextInt(5);
        switch (index) {
            case 0 -> sms.setMoisturePercentage(random.nextBoolean() ? sms.getMoisturePercentage() + 1 : sms.getMoisturePercentage() - 1);
            case 1 -> sms.setSoilTemperature(random.nextBoolean() ? sms.getSoilTemperature() + 1 : sms.getSoilTemperature() - 1);
            case 2 -> sms.setBatteryLevel(Math.max(0, sms.getBatteryLevel() - 1));
            case 3 -> sms.setStatus(updateStatus(random));
            case 4 -> sms.setFirmwareVersion(updateFirmwareVersion(sms.getFirmwareVersion()));
        }
        sms.setLastUpdated(now());
        return sms;
    }
}
