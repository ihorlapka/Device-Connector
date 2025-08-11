package com.iot.device_connector.rest;

import com.iot.device_connector.rest.enums.DeviceManufacturer;
import com.iot.device_connector.rest.enums.DeviceStatus;
import com.iot.device_connector.rest.enums.DeviceType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record Device(UUID id,
                     String name,
                     String serialNumber,
                     DeviceManufacturer deviceManufacturer,
                     String model,
                     DeviceType deviceType,
                     String location,
                     BigDecimal latitude,
                     BigDecimal longitude,
                     UUID ownerId,
                     DeviceStatus status,
                     OffsetDateTime lastActiveAt,
                     String firmwareVersion,
                     OffsetDateTime createdAt,
                     OffsetDateTime updatedAt) {
}
