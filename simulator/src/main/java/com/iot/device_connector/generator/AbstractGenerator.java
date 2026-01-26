package com.iot.device_connector.generator;

import com.iot.device_connector.auth.AuthenticationRequest;
import com.iot.device_connector.auth.AuthenticationResponse;
import com.iot.device_connector.kafka.TelemetriesKafkaProducerRunner;
import com.iot.device_connector.model.Device;
import com.iot.device_connector.model.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static java.lang.Thread.sleep;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractGenerator {

    public static final String USERS_URL = "/iot-registry/api/v1/users/all";
    public static final String REGISTRY_BASE_URL = System.getenv("REGISTRY_BASE_URL");
    private static final String LOGIN_URL = "/iot-registry/api/v1/authentication/login";
    private static final String LOGOUT_URL = "/iot-registry/api/v1/authentication/logout";
    private static final String SIZE = "size";
    private static final String PAGE = "page";
    private static final int ONE_MINUTE_MS = 60_000;
    private static final String USERNAME = System.getenv("USERNAME");
    private static final String PASSWORD = System.getenv("PASSWORD");
    static final String TOKEN_PREFIX = "Bearer ";

    @Getter
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    private final RestTemplate restTemplate;
    private final TelemetriesKafkaProducerRunner kafkaProducerRunner;
    private final TelemetryCreator telemetryCreator;

    abstract String getLoadingUri();

    public void start() {
        isStarted.set(true);
    }

    public void stop() {
        isStarted.set(false);
    }

    <U> List<Device> loadDevices(AuthenticationResponse authResponse, String uriParam,
                                 ParameterizedTypeReference<U> responseType,
                                 Function<U, List<Device>> devicesFromUsersFunction) {
        final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(REGISTRY_BASE_URL + getLoadingUri() + uriParam)
                .queryParam(SIZE, 40)
                .queryParam(PAGE, 0);

        final HttpEntity<?> requestEntity = buildHttpEntity(authResponse, builder);
        final ResponseEntity<U> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                requestEntity,
                responseType
        );
        final U users = response.getBody();
        assert (users != null);
        return devicesFromUsersFunction.apply(users);
    }

    Function<List<User>, List<Device>> getDevicesFromManyUsersFunction() {
        return users ->  {
            final List<Device> devices = users.stream()
                    .map(User::devices)
                    .flatMap(Collection::stream)
                    .toList();
            log.info("Received {} users with {} devices", users.size(), devices.size());
            return devices;
        };
    }

    AuthenticationResponse login() {
        final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(REGISTRY_BASE_URL + LOGIN_URL);
        final AuthenticationRequest request = new AuthenticationRequest(USERNAME, PASSWORD);
        ResponseEntity<AuthenticationResponse> authResponse = restTemplate.postForEntity(builder.toUriString(), request, AuthenticationResponse.class, Map.of());
        log.info("Simulator app is logged in!");
        return authResponse.getBody();
    }

    void logout(AuthenticationResponse authResponse) {
        final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(REGISTRY_BASE_URL + LOGOUT_URL);
        HttpEntity<?> httpEntity = buildHttpEntity(authResponse, builder);
        restTemplate.postForEntity(builder.toUriString(), httpEntity, Void.class, Map.of());
        log.info("Simulator app is logged out!");
    }

    private HttpEntity<?> buildHttpEntity(AuthenticationResponse authResponse, UriComponentsBuilder builder) {
        final MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add(AUTHORIZATION, TOKEN_PREFIX + authResponse.getAccessToken());
        return new RequestEntity<>(headers, HttpMethod.GET, builder.build(Map.of()));
    }

    void createTelemetries(List<Device> devices, AtomicInteger rpm) {
        while (isStarted.get()) {
            try {
                for (Device device : devices) {
                    if (!isStarted.get()) {
                        log.info("Stopped generating telemetries");
                        break;
                    }
                    final int timeout = ONE_MINUTE_MS / rpm.get();
                    final SpecificRecord record = telemetryCreator.create(device);
                    log.info("Sending message, current time: {}, timeout: {} ms, {}", now().truncatedTo(SECONDS), timeout, record);
                    kafkaProducerRunner.sendMessage(device.id().toString(), record);
                    sleep(timeout);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        log.info("Telemetries are stopped...");
    }
}
