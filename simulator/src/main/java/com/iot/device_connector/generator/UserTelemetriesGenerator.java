package com.iot.device_connector.generator;

import com.iot.device_connector.auth.AuthenticationResponse;
import com.iot.device_connector.kafka.TelemetriesKafkaProducerRunner;
import com.iot.device_connector.model.Device;
import com.iot.device_connector.model.User;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Slf4j
@Component
public class UserTelemetriesGenerator extends AbstractGenerator {

    private static final String USERNAME_URL = "/iot-registry/api/v1/users/username/";

    private final ConcurrentHashMap<String, AtomicInteger> rpmByUsername = new ConcurrentHashMap<>();

    public UserTelemetriesGenerator(RestTemplate restTemplate, TelemetriesKafkaProducerRunner kafkaProducerRunner, TelemetryCreator telemetryCreator) {
        super(restTemplate, kafkaProducerRunner, telemetryCreator);
    }

    public void changeWithRpmForUser(String username, int newRpm) {
        rpmByUsername.computeIfPresent(username, (k, v) -> {
            v.set(newRpm);
            return v;
        });
    }

    public void startForDevices(@NonNull String username, @NonNull List<UUID> desiredDeviceIds, int rpm) {
        final AuthenticationResponse authResponse = login();
        final List<Device> allUserDevices = loadDevices(authResponse, username, getDevicesFromOneUserFunction());
        logout(authResponse);
        final List<Device> desiredDevices = new ArrayList<>(desiredDeviceIds.size());
        for (Device device : allUserDevices) {
            if (desiredDeviceIds.contains(device.id())) {
                desiredDevices.add(device);
            }
        }
        final AtomicInteger userRpm = new AtomicInteger(rpm);
        rpmByUsername.put(username, userRpm);
        log.info("Generating telemetries for user={}, with rpm={}, deviceIds={}", username, rpm, desiredDeviceIds);
        createTelemetries(desiredDevices, rpmByUsername.get(username));
    }

    private Function<User, List<Device>> getDevicesFromOneUserFunction() {
        return user ->  {
            log.info("Received user with {} devices", user.devices().size());
            return new ArrayList<>(user.devices());
        };
    }

    @Override
    String getLoadingUrl() {
        return USERNAME_URL;
    }
}
