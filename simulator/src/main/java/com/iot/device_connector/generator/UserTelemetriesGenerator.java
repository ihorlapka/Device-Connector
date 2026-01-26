package com.iot.device_connector.generator;

import com.iot.device_connector.auth.AuthenticationResponse;
import com.iot.device_connector.kafka.TelemetriesKafkaProducerRunner;
import com.iot.device_connector.model.Device;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class UserTelemetriesGenerator extends AbstractGenerator {

    private static final String USERNAME_URL = "/iot-registry/api/v1/users/username";

    private final ConcurrentHashMap<String, AtomicInteger> rpmByUsername = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CountDownLatch> latchByUsername = new ConcurrentHashMap<>();

    public UserTelemetriesGenerator(RestTemplate restTemplate, TelemetriesKafkaProducerRunner kafkaProducerRunner,
                                    TelemetryCreator telemetryCreator) {
        super(restTemplate, kafkaProducerRunner, telemetryCreator);
    }

    public void startWithRpmForUser(String username, int newRpm) {
        rpmByUsername.computeIfPresent(username, (k, v) -> {
            v.set(newRpm);
            return v;
        });
        if (latchByUsername.get(username).getCount() == 1) {
            log.info("Released latch for user: {}, set rpm to {}", username, newRpm);
            latchByUsername.get(username).countDown();
        } else {
            log.info("Changed rpm={} for user: {}", username, latchByUsername.get(username).getCount());
        }
    }

    public void startForDevices(@NonNull String username, @NonNull List<UUID> desiredDeviceIds, int rpm) {
        final AuthenticationResponse authResponse = login();
        final List<Device> allUserDevices = loadDevices(authResponse, username);
        logout(authResponse);
        final List<Device> desiredDevices = new ArrayList<>(desiredDeviceIds.size());
        for (Device device : allUserDevices) {
            if (desiredDeviceIds.contains(device.id())) {
                desiredDevices.add(device);
            }
        }
        final AtomicInteger userRpm = new AtomicInteger(rpm);
        rpmByUsername.put(username, userRpm);
        latchByUsername.put(username, new CountDownLatch(1));
        log.info("Generating telemetries for user={}, with rpm={}, deviceIds={}", username, rpm, desiredDeviceIds);
        createTelemetries(desiredDevices, latchByUsername.get(username), rpmByUsername.get(username));
    }

    @Override
    String getLoadingUrl() {
        return USERNAME_URL;
    }
}
