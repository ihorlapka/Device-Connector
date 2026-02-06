package com.iot.device_connector.devices;

import com.iot.device_connector.auth.RegistryAuthenticator;
import com.iot.device_connector.devices.dto.DeviceDto;
import com.iot.device_connector.model.Device;
import com.iot.device_connector.model.enums.DeviceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.iot.device_connector.generator.AbstractGenerator.REGISTRY_BASE_URL;
import static com.iot.device_connector.generator.AbstractGenerator.TOKEN_PREFIX;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Component
@RequiredArgsConstructor
public class DevicesProvider {

    private final RestTemplate restTemplate;
    private final RegistryAuthenticator authenticator;
    private final TelemetryParser telemetryParser;

    private final ConcurrentHashMap<String, DeviceDto> deviceByDeviceId = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends DeviceDto> Optional<T> getDevice(DeviceType desiredDeviceType, String deviceId, Class<T> clazz) {
        final T device = (T) getOrLoadDevice(deviceId, clazz);
        if (device == null) {
            return Optional.empty();
        }
        if (!desiredDeviceType.equals(device.getDeviceType())) {
            log.warn("Inappropriate device types deviceId={}, desired={}, db={}", deviceId, desiredDeviceType, device.getDeviceType());
            throw new RuntimeException("Device types mismatch!");
        }
        return Optional.of(device);
    }

    public void updateDevice(DeviceDto device) {
        deviceByDeviceId.put(device.getDeviceId().toString(), device);
    }

    private <T extends DeviceDto> DeviceDto getOrLoadDevice(String deviceId, Class<T> clazz) {
        return deviceByDeviceId.computeIfAbsent(deviceId, (k) -> loadDevice(deviceId, clazz));
    }

    private <T extends DeviceDto> T loadDevice(String deviceId, Class<T> clazz) {
        final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(REGISTRY_BASE_URL + deviceId);

        log.info("Calling {}", builder.toUriString());
        final ResponseEntity<Device> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                buildHttpEntity(builder),
                Device.class
        );
        final Device device = response.getBody();
        log.info("Received response on call: {} is {}", builder.toUriString(), device);
        if (device == null) {
            log.info("Device with id={} is not found!", deviceId);
            return null;
        }
        final T dto = telemetryParser.parse(device, clazz);
        deviceByDeviceId.put(deviceId, dto);
        return dto;
    }

    private HttpEntity<?> buildHttpEntity(UriComponentsBuilder builder) {
        final MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add(AUTHORIZATION, TOKEN_PREFIX + authenticator.login().getAccessToken());
        return new RequestEntity<>(headers, HttpMethod.GET, builder.build(Map.of()));
    }
}
