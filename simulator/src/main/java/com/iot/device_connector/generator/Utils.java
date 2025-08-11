package com.iot.device_connector.generator;

import com.iot.devices.DeviceStatus;
import com.iot.devices.DoorSensor;
import lombok.experimental.UtilityClass;

import java.util.Random;

import static com.iot.devices.DeviceStatus.*;


@UtilityClass
public class Utils {

    public static String updateFirmwareVersion(String firmwareVersion) {
        int length = firmwareVersion.length();
        int updatedDigit = Integer.parseInt(String.valueOf(firmwareVersion.charAt(length - 1))) + 1;
        String substring = firmwareVersion.substring(0, length - 1);
        return substring + updatedDigit;
    }

    public static DeviceStatus updateStatus(Random random) {
        int i = random.nextInt(20);
        return switch (i) {
            case 0 -> OFFLINE;
            case 1 -> ERROR;
            case 2 -> MAINTENANCE;
            default -> ONLINE;
        };
    }
}
