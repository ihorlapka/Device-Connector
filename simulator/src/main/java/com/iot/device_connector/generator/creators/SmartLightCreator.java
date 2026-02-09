package com.iot.device_connector.generator.creators;

import com.iot.device_connector.model.Device;
import com.iot.devices.SmartLight;
import com.iot.devices.SmartLightMode;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static com.iot.device_connector.generator.Utils.*;
import static com.iot.devices.DeviceStatus.ONLINE;
import static com.iot.devices.SmartLightMode.AMBIENT;
import static java.time.Instant.now;

@Component
@RequiredArgsConstructor
public class SmartLightCreator {

    public static final boolean SMART_LIGHT_IS_ON_DEFAULT = true;
    public static final int SMART_LIGHT_BRIGHTNESS_DEFAULT = 2;
    public static final String SMART_LIGHT_COLOUR_DEFAULT = "Yellow";
    public static final SmartLightMode SMART_LIGHT_MODE_DEFAULT = AMBIENT;
    public static final float SMART_LIGHT_POWER_CONSUMPTION_DEFAULT = 8f;

    private final Random random = new Random();
    private final ConcurrentHashMap<String, SmartLight> telemetriesById = new ConcurrentHashMap<>();

    public SpecificRecord create(Device device) {
        if (!telemetriesById.containsKey(device.id().toString())) {
            final SmartLight smartLight = SmartLight.newBuilder()
                    .setDeviceId(device.id().toString())
                    .setIsOn(SMART_LIGHT_IS_ON_DEFAULT)
                    .setBrightness(SMART_LIGHT_BRIGHTNESS_DEFAULT)
                    .setColor(SMART_LIGHT_COLOUR_DEFAULT)
                    .setMode(SMART_LIGHT_MODE_DEFAULT)
                    .setPowerConsumption(SMART_LIGHT_POWER_CONSUMPTION_DEFAULT)
                    .setStatus(ONLINE)
                    .setFirmwareVersion("10.2.0")
                    .setLastUpdated(now())
                    .build();
            telemetriesById.put(smartLight.getDeviceId(), smartLight);
            return smartLight;
        } else {
            return telemetriesById.computeIfPresent(device.id().toString(), (id, smartLight) -> updateTelemetries(smartLight));
        }
    }

    private SmartLight updateTelemetries(SmartLight smartLight) {
        final int index = random.nextInt(6);
        switch (index) {
            case 0 -> smartLight.setIsOn(!smartLight.getIsOn());
            case 1 -> smartLight.setBrightness(smartLight.getBrightness() + random.nextInt(-3, 4));
            case 2 -> smartLight.setMode(SmartLightMode.values()[random.nextInt(SmartLightMode.values().length)]);
            case 3 -> smartLight.setPowerConsumption(smartLight.getPowerConsumption() + getRandomRoundedFloat(-2, 3));
            case 4 -> smartLight.setStatus(updateStatus(random));
            case 5 -> smartLight.setFirmwareVersion(updateFirmwareVersion(smartLight.getFirmwareVersion()));
        }
        smartLight.setLastUpdated(now());
        return smartLight;
    }
}
