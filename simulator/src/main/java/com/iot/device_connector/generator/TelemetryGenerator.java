package com.iot.device_connector.generator;

import com.iot.device_connector.auth.AuthenticationResponse;
import com.iot.device_connector.kafka.TelemetriesKafkaProducerRunner;
import com.iot.device_connector.model.Device;
import com.iot.device_connector.model.User;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class TelemetryGenerator extends AbstractGenerator {

    private final AtomicInteger rpm = new AtomicInteger();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public TelemetryGenerator(RestTemplate restTemplate, TelemetriesKafkaProducerRunner kafkaProducerRunner,
                              TelemetryCreator telemetryCreator) {
        super(restTemplate, kafkaProducerRunner, telemetryCreator);
    }

    public void startWithRpm(int newRpm) {
        log.info("Setting new rpm={}", newRpm);
        rpm.set(newRpm);
        if (!getIsStarted().get()) {
            start();
            executorService.submit(this::generate);
        }
    }

    private void generate() {
        final AuthenticationResponse authResponse = login();
        final List<Device> devices = loadDevices(authResponse, "",
                new ParameterizedTypeReference<List<User>>() {}, getDevicesFromManyUsersFunction());
        logout(authResponse);
        log.info("Generating telemetries for all devices with rpm={}", rpm);
        createTelemetries(devices, rpm);
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

    @Override
    String getLoadingUri() {
        return USERS_URL;
    }
}
