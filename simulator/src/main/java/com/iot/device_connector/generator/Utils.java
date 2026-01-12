package com.iot.device_connector.generator;

import com.iot.devices.DeviceStatus;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.Random;

import static com.iot.devices.DeviceStatus.*;
import static java.math.RoundingMode.HALF_UP;


@UtilityClass
public class Utils {

    private static final Random random = new Random();

    public static String updateFirmwareVersion(String firmwareVersion) {
//        int length = firmwareVersion.length();
//        int updatedDigit = Integer.parseInt(String.valueOf(firmwareVersion.charAt(length - 1))) + 1;
//        String substring = firmwareVersion.substring(0, length - 1);
//        return substring + updatedDigit;
        return firmwareVersion;
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

    public static float getRandomRoundedFloat(int from, int to) {
        return BigDecimal.valueOf(random.nextFloat(from, to)).setScale(2, HALF_UP).floatValue();
    }
}
