package com.iot.device_connector.generator;

import com.iot.device_connector.auth.AuthenticationResponse;
import com.iot.device_connector.kafka.TelemetriesKafkaProducerRunner;
import com.iot.device_connector.model.Device;
import com.iot.device_connector.model.User;
import jakarta.annotation.PreDestroy;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Slf4j
@Component
public class UserTelemetriesGenerator extends AbstractGenerator {

    private static final String USERNAME_URL = "/iot-registry/api/v1/users/username/";

    private final AtomicInteger rpm = new AtomicInteger();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public UserTelemetriesGenerator(RestTemplate restTemplate, TelemetriesKafkaProducerRunner kafkaProducerRunner, TelemetryCreator telemetryCreator) {
        super(restTemplate, kafkaProducerRunner, telemetryCreator);
    }

    public void startWithRpm(@NonNull String username, @NonNull List<UUID> desiredDeviceIds, int newRpm) {
        log.info("Setting new rpm={} for user={}", newRpm, username);
        rpm.set(newRpm);
        if (!getIsStarted().get()) {
            start();
            executorService.submit(() -> generate(username, desiredDeviceIds));
        }
    }

    private void generate(String username, List<UUID> desiredDeviceIds) {
        final AuthenticationResponse authResponse = login();
        final List<Device> devices = loadDevices(authResponse, username,
                new ParameterizedTypeReference<User>() {}, getDevicesFromOneUserFunction());
        logout(authResponse);
        final List<Device> desiredDevices = new ArrayList<>(desiredDeviceIds.size());
        for (Device device : devices) {
            if (desiredDeviceIds.contains(device.id())) {
                desiredDevices.add(device);
            }
        }
        log.info("Generating telemetries for user={}, with rpm={}, deviceIds={}", username, rpm, desiredDeviceIds);
        createTelemetries(desiredDevices, rpm);
    }

    private Function<User, List<Device>> getDevicesFromOneUserFunction() {
        return user ->  {
            log.info("Received user with {} devices", user.devices().size());
            return new ArrayList<>(user.devices());
        };
    }

    @Override
    String getLoadingUri() {
        return USERNAME_URL;
    }

    @PreDestroy
    private void closeExecutor() throws InterruptedException {
        executorService.shutdown();
        if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
            executorService.shutdownNow();
            log.info("Executor shutdown forced");
        } else {
            log.info("Executor shutdown gracefully");
        }
    }
}
