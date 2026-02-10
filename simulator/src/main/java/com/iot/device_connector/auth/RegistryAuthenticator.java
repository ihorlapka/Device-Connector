package com.iot.device_connector.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.iot.device_connector.generator.AbstractGenerator.REGISTRY_BASE_URL;
import static com.iot.device_connector.generator.AbstractGenerator.TOKEN_PREFIX;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegistryAuthenticator {

    private static final String LOGIN_URL = "/iot-registry/api/v1/authentication/login";
    private static final String LOGOUT_URL = "/iot-registry/api/v1/authentication/logout";
    private static final String USERNAME = System.getenv("USERNAME");
    private static final String PASSWORD = System.getenv("PASSWORD");

    private final RestTemplate restTemplate;

    private final AtomicReference<AuthenticationResponse> authentication = new AtomicReference<>();

    public AuthenticationResponse login() {
        if (authentication.get() != null) {
            return authentication.get();
        }
        final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(REGISTRY_BASE_URL + LOGIN_URL);
        final AuthenticationRequest request = new AuthenticationRequest(USERNAME, PASSWORD);
        ResponseEntity<AuthenticationResponse> authResponse = restTemplate.postForEntity(builder.toUriString(), request, AuthenticationResponse.class, Map.of());
        log.info("Simulator app is logged in!");
        authentication.set(authResponse.getBody());
        return authResponse.getBody();
    }

    public void logout(AuthenticationResponse authResponse) {
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
}
