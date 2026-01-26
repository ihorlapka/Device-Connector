package com.iot.device_connector.generator;

import com.iot.device_connector.auth.AuthenticationResponse;
import com.iot.device_connector.kafka.TelemetriesKafkaProducerRunner;
import com.iot.device_connector.model.Device;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

@Slf4j
@Component
public class TelemetryGenerator extends AbstractGenerator {

    private final AtomicInteger rpm = new AtomicInteger();
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public TelemetryGenerator(RestTemplate restTemplate, TelemetriesKafkaProducerRunner kafkaProducerRunner,
                              TelemetryCreator telemetryCreator) {
        super(restTemplate, kafkaProducerRunner, telemetryCreator);
    }

    @PostConstruct
    public void generate() {
        final AuthenticationResponse authResponse = login();
        final List<Device> devices = loadDevices(authResponse, "", getDevicesFromManyUsersFunction());
        executorService.submit(() -> {
            try {
                lockUntilTriggered();
                createTelemetries(devices, rpm);
            } finally {
                logout(authResponse);
            }
        });
    }

    public void startWithRpm(int newRpm) {
        rpm.set(newRpm);
        if (countDownLatch.getCount() == 1) {
            log.info("Released latch, set rpm to {}", newRpm);
            countDownLatch.countDown();
        } else {
            log.info("Changed rpm={}", countDownLatch.getCount());
        }
    }

    private void lockUntilTriggered() {
        try {
            if (countDownLatch.getCount() > 0) {
                log.info("Current rpm={} waiting until it is changed manually", rpm);
                countDownLatch.await();
            }
            sleep(1000);
        } catch (InterruptedException e) {
            log.error("Interrupted exception occurred");
            throw new RuntimeException(e);
        }
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
    String getLoadingUrl() {
        return USERS_URL;
    }
}
