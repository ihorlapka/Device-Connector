package com.iot.device_connector.generator.creators;

import com.iot.device_connector.model.Device;
import com.iot.devices.SmartPlug;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static com.iot.device_connector.generator.Utils.*;
import static com.iot.devices.DeviceStatus.ONLINE;
import static java.time.Instant.now;

@Component
@RequiredArgsConstructor
public class SmartPlugCreator {

    private final Random random = new Random();
    private final ConcurrentHashMap<String, SmartPlug> telemetriesById = new ConcurrentHashMap<>();

    public SpecificRecord create(Device device) {
        if (!telemetriesById.containsKey(device.id().toString())) {
            final SmartPlug smartPlug = SmartPlug.newBuilder()
                    .setDeviceId(device.id().toString())
                    .setIsOn(true)
                    .setVoltage(220f)
                    .setCurrent(8f)
                    .setPowerUsage(1760f)
                    .setStatus(ONLINE)
                    .setFirmwareVersion("5.4.0")
                    .setLastUpdated(now())
                    .build();
            telemetriesById.put(smartPlug.getDeviceId(), smartPlug);
            return smartPlug;
        } else {
            return telemetriesById.computeIfPresent(device.id().toString(), (id, smartPlug) -> updateTelemetries(smartPlug));
        }
    }

    private SmartPlug updateTelemetries(SmartPlug smartLight) {
        final int index = random.nextInt(10);
        switch (index) {
            case 0 -> smartLight.setIsOn(!smartLight.getIsOn());
            case 1 -> smartLight.setStatus(updateStatus(random));
            case 2 -> smartLight.setFirmwareVersion(updateFirmwareVersion(smartLight.getFirmwareVersion()));
            default -> {
                if (smartLight.getIsOn()) {
                    smartLight.setVoltage(smartLight.getVoltage() + getRandomRoundedFloat(-2, 3));
                    smartLight.setCurrent(smartLight.getCurrent() + getRandomRoundedFloat(-1, 2));
                    smartLight.setPowerUsage(smartLight.getVoltage() * smartLight.getCurrent());
                } else {
                    smartLight.setIsOn(true);
                }
            }
        }
        smartLight.setLastUpdated(now());
        return smartLight;
    }
}
