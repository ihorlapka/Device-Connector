package com.iot.device_connector.generator;

import com.iot.device_connector.rest.Device;
import com.iot.devices.DoorSensor;
import com.iot.devices.EnergyMeter;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static com.iot.devices.DeviceStatus.*;
import static com.iot.devices.DeviceStatus.ONLINE;
import static com.iot.devices.DoorState.CLOSED;
import static com.iot.devices.DoorState.OPEN;
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
//                    .setVoltage()
//                    .setCurrent()
//                    .setPower()
//                    .setEnergyConsumed()
//                    .setStatus()
//                    .setFirmwareVersion()
                    .setLastUpdated(now())
                    .build();
            telemetriesById.put(energyMeter.getDeviceId(), energyMeter);
            return energyMeter;
        } else {
            return telemetriesById.computeIfPresent(device.id().toString(), (id, energyMeter) -> updateTelemetries(energyMeter));
        }
    }

    private EnergyMeter updateTelemetries(EnergyMeter energyMeter) {
        final int index = random.nextInt(5);
//        switch (index) {
//            case 0 -> {
//                final boolean wasOpen = energyMeter.getDoorState().equals(OPEN);
//                energyMeter.setDoorState(wasOpen ? CLOSED : OPEN);
//                if (!wasOpen) {
//                    energyMeter.setLastOpened(now());
//                }
//            }
//            case 1 -> energyMeter.setBatteryLevel(Math.max(0, energyMeter.getBatteryLevel() - 1));
//            case 2 -> energyMeter.setTamperAlert((random.nextInt(9) == 0)) ;
//            case 3 -> {
//                int i = random.nextInt(20);
//                if (i == 0) {
//                    energyMeter.setStatus(OFFLINE);
//                } else if (i == 1) {
//                    energyMeter.setStatus(MAINTENANCE);
//                } else {
//                    energyMeter.setStatus(ONLINE);
//                }
//            }
//            case 4 -> {
//                String firmwareVersion = energyMeter.getFirmwareVersion();
//                int length = firmwareVersion.length();
//                int updatedDigit = Integer.parseInt(String.valueOf(firmwareVersion.charAt(length - 1))) + 1;
//                String substring = firmwareVersion.substring(0, length - 1);
//                String newVersion = substring + updatedDigit;
//                energyMeter.setFirmwareVersion(newVersion);
//            }
//        }
//        energyMeter.setLastUpdated(now());
        return energyMeter;
    }
}
