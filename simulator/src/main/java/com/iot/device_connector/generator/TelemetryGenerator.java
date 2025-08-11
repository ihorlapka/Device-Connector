package com.iot.device_connector.generator;

import com.iot.device_connector.kafka.KafkaProducerRunner;
import com.iot.device_connector.model.Device;
import com.iot.device_connector.model.User;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.SECONDS;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelemetryGenerator {

    private static final String URL = "http://localhost:8080/iot-registry/api/v1/users";
    private static final String SIZE = "size";
    private static final String PAGE = "page";
    private static final int ONE_MINUTE_MS = 60_000;

    private final AtomicInteger rpm = new AtomicInteger();
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private final AtomicBoolean isShoutdown = new AtomicBoolean();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final RestTemplate restTemplate;
    private final KafkaProducerRunner kafkaProducerRunner;
    private final TelemetryCreator telemetryCreator;


    @PostConstruct
    public void generate() {
        final List<Device> devices = loadDevices();
        executorService.submit(() -> createTelemetries(devices));
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

    public void shutdown() {
        isShoutdown.set(true);
    }

    private List<Device> loadDevices() {
        final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(URL).queryParam(SIZE, 30).queryParam(PAGE, 0);
        final ResponseEntity<List<User>> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        final List<User> users = response.getBody();
        assert (users != null);
        final List<Device> devices = users.stream()
                .map(User::devices)
                .flatMap(Collection::stream)
                .toList();
        log.info("Received {} users with {} devices", users.size(), devices.size());
        return devices;
    }

    private void createTelemetries(List<Device> devices) {
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
        while (!isShoutdown.get()) {
            try {
                for (Device device : devices) {
                    final int timeout = ONE_MINUTE_MS / rpm.get();
                    final SpecificRecord record = telemetryCreator.create(device);
                    log.info("Sending message, current time: {}, timeout: {} ms", now().truncatedTo(SECONDS), timeout);
                    kafkaProducerRunner.sendMessage(device.id().toString(), record);
                    sleep(timeout);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        log.info("Shutting down...");
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
