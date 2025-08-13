package com.iot.device_connector.generator.creators;

import com.iot.device_connector.model.Device;
import com.iot.devices.DeviceStatus;
import com.iot.devices.EnergyMeter;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static com.iot.device_connector.generator.Utils.updateFirmwareVersion;
import static com.iot.device_connector.generator.Utils.updateStatus;
import static com.iot.devices.DeviceStatus.OFFLINE;
import static com.iot.devices.DeviceStatus.ONLINE;
import static java.time.Instant.now;

@Component
@RequiredArgsConstructor
public class EnergyMeterCreator {

    private final Random random = new Random();
    private final ConcurrentHashMap<String, EnergyMeter> telemetriesById = new ConcurrentHashMap<>();

    public SpecificRecord create(Device device) {
        if (!telemetriesById.containsKey(device.id().toString())) {
            final EnergyMeter energyMeter = EnergyMeter.newBuilder()
                    .setDeviceId(device.id().toString())
                    .setVoltage(230f)
                    .setCurrent(4f)
                    .setPower(800f)
                    .setEnergyConsumed(0f)
                    .setStatus(ONLINE)
                    .setFirmwareVersion("2.1")
                    .setLastUpdated(now())
                    .build();
            telemetriesById.put(energyMeter.getDeviceId(), energyMeter);
            return energyMeter;
        } else {
            return telemetriesById.computeIfPresent(device.id().toString(), (id, energyMeter) -> updateTelemetries(energyMeter));
        }
    }

    private EnergyMeter updateTelemetries(EnergyMeter energyMeter) {
        final int index = random.nextInt(10);
        switch (index) {
            case 0 -> {
                DeviceStatus status = updateStatus(random);
                energyMeter.setStatus(status);
                if (status.equals(ONLINE) && energyMeter.getStatus().equals(OFFLINE)) {
                    energyMeter.setEnergyConsumed(0f);
                }
            }
            case 1 -> energyMeter.setFirmwareVersion(updateFirmwareVersion(energyMeter.getFirmwareVersion()));
            default -> {
                energyMeter.setVoltage(220 + random.nextFloat(-10, 11));
                energyMeter.setCurrent(random.nextFloat(1, 10));
                energyMeter.setPower(energyMeter.getVoltage() * energyMeter.getCurrent());
                energyMeter.setEnergyConsumed(energyMeter.getEnergyConsumed() + energyMeter.getPower());
            }
        }
        energyMeter.setLastUpdated(now());
        return energyMeter;
    }
}
