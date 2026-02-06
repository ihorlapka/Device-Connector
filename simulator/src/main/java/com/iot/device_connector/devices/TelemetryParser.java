package com.iot.device_connector.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.device_connector.devices.dto.DeviceDto;
import com.iot.device_connector.model.Device;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelemetryParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public <T extends DeviceDto> T parse(Device device, Class<T> clazz) { //todo: refactor!
        try {
            T telemetry = objectMapper.readValue(device.telemetry(), clazz);
            return telemetry;
        } catch (Exception e) {
            log.warn("Unable to parse telemetry: {}, deviceId={}",  device.telemetry(), device.id());
            throw new RuntimeException(e);
        }
    }
}
