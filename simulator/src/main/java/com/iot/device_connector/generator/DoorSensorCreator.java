package com.iot.device_connector.generator;

import com.iot.device_connector.rest.Device;
import com.iot.devices.DoorSensor;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static com.iot.device_connector.generator.Utils.updateFirmwareVersion;
import static com.iot.devices.DeviceStatus.*;
import static com.iot.devices.DoorState.CLOSED;
import static com.iot.devices.DoorState.OPEN;
import static java.time.Instant.now;

@Component
@RequiredArgsConstructor
public class DoorSensorCreator {

    private final Random random = new Random();
    private final ConcurrentHashMap<String, DoorSensor> telemetriesById = new ConcurrentHashMap<>();

    public SpecificRecord create(Device device) {
        if (!telemetriesById.containsKey(device.id().toString())) {
            final DoorSensor doorSensor = DoorSensor.newBuilder()
                    .setDeviceId(device.id().toString())
                    .setDoorState(CLOSED)
                    .setBatteryLevel(100)
                    .setTamperAlert(false)
                    .setStatus(ONLINE)
                    .setLastOpened(null)
                    .setFirmwareVersion("1.0.1")
                    .setLastUpdated(now())
                    .build();
            telemetriesById.put(doorSensor.getDeviceId(), doorSensor);
            return doorSensor;
        } else {
            return telemetriesById.computeIfPresent(device.id().toString(), (id, doorSensor) -> updateTelemetries(doorSensor));
        }
    }

    private DoorSensor updateTelemetries(DoorSensor doorSensor) {
        final int index = random.nextInt(5);
        switch (index) {
            case 0 -> {
                final boolean wasOpen = doorSensor.getDoorState().equals(OPEN);
                doorSensor.setDoorState(wasOpen ? CLOSED : OPEN);
                if (!wasOpen) {
                    doorSensor.setLastOpened(now());
                }
            }
            case 1 -> doorSensor.setBatteryLevel(Math.max(0, doorSensor.getBatteryLevel() - 1));
            case 2 -> doorSensor.setTamperAlert((random.nextInt(9) == 0)) ;
            case 3 -> {
                int i = random.nextInt(20);
                if (i == 0) {
                    doorSensor.setStatus(OFFLINE);
                } else if (i == 1) {
                    doorSensor.setStatus(MAINTENANCE);
                } else {
                    doorSensor.setStatus(ONLINE);
                }
            }
            case 4 -> updateFirmwareVersion(doorSensor);
        }
        doorSensor.setLastUpdated(now());
        return doorSensor;
    }
}
