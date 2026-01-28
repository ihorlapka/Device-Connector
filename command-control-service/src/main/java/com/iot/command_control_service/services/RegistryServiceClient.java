package com.iot.command_control_service.services;

import com.iot.command_control_service.controller.CommandRequest;
import com.iot.command_control_service.controller.PermissionToDeviceResponse;
import com.iot.command_control_service.controller.errors.AuthorizationHeaderNotPresentException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegistryServiceClient {

    private static final String REGISTRY_BASE_URL = System.getenv("REGISTRY_BASE_URL");
    public static final String PERMISSION_CHECK_URL = "/iot-registry/api/v1/devices/permission/";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final RestTemplate restTemplate;


    public boolean checkAccess(CommandRequest commandRequest, HttpServletRequest httpServletRequest) {
        final String authHeader = httpServletRequest.getHeader(AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
            log.info("No authorization header is present in request, path={}", httpServletRequest.getServletPath());
            throw new AuthorizationHeaderNotPresentException();
        }
        final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(REGISTRY_BASE_URL + PERMISSION_CHECK_URL + commandRequest.deviceId());
        final HttpEntity<?> requestEntity = buildHttpEntity(authHeader, builder);
        final ResponseEntity<PermissionToDeviceResponse> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                requestEntity,
                PermissionToDeviceResponse.class
        );
        log.info("Received response from registry service {}", response.getBody());
        if (response.getBody() == null) {
            throw new RuntimeException("Empty response, server error");
        }
        return response.getBody().accessAllowed();
    }

    private HttpEntity<?> buildHttpEntity(String authHeader, UriComponentsBuilder builder) {
        final MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add(AUTHORIZATION, authHeader);
        return new RequestEntity<>(headers, HttpMethod.GET, builder.build(Map.of()));
    }
}
