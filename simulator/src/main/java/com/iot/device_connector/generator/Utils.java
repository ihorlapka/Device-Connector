package com.iot.device_connector.generator;

import com.iot.devices.DoorSensor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Utils {

    public static void updateFirmwareVersion(DoorSensor doorSensor) {
        String firmwareVersion = doorSensor.getFirmwareVersion();
        int length = firmwareVersion.length();
        int updatedDigit = Integer.parseInt(String.valueOf(firmwareVersion.charAt(length - 1))) + 1;
        String substring = firmwareVersion.substring(0, length - 1);
        String newVersion = substring + updatedDigit;
        doorSensor.setFirmwareVersion(newVersion);
    }
}
