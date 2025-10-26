package com.iot.device_connector.generator;

import com.iot.device_connector.auth.AuthenticationResponse;
import com.iot.device_connector.generator.alerts.*;
import com.iot.device_connector.model.Device;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.iot.device_connector.generator.TelemetryGenerator.TOKEN_PREFIX;
import static com.iot.device_connector.generator.alerts.MetricType.BATTERY_LEVEL;
import static com.iot.device_connector.generator.alerts.ThresholdType.LESS_THAN;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertingRulesCreator {

    private final RestTemplate restTemplate;

    private static final String ALERT_RULES_URL = "http://localhost:8080/iot-registry/api/v1/alertRules";

    public void create(List<Device> devices, AuthenticationResponse authResponse) {
        for (Device device : devices) {
            final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(ALERT_RULES_URL);
            List<RequestEntity<CreateAlertRuleRequest>> requestEntities = buildHttpEntities(device, authResponse, builder);

            for (RequestEntity<CreateAlertRuleRequest> request : requestEntities) {
                ResponseEntity<AlertRuleResponse> response = restTemplate.postForEntity(builder.toUriString(), request, AlertRuleResponse.class, Map.of());
                log.info("Response: {}", response.getBody());
            }
        }
    }

    private List<RequestEntity<CreateAlertRuleRequest>> buildHttpEntities(Device device, AuthenticationResponse authResponse, UriComponentsBuilder builder) {
        final MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add(AUTHORIZATION, TOKEN_PREFIX + authResponse.getAccessToken());
        return createAlertRuleRequests(device).stream()
                .map(request -> new RequestEntity<>(request, headers, HttpMethod.POST, builder.build(Map.of())))
                .toList();
    }

    private List<CreateAlertRuleRequest> createAlertRuleRequests(Device device) {
        return switch (device.deviceType()) {
            case DOOR_SENSOR -> createDoorSensorStandardAlertRules(device);
            case ENERGY_METER -> createEnergyMeterStandardAlertRules(device);
            default -> throw new IllegalArgumentException("Unknown device type!");
        };
    }

    private List<CreateAlertRuleRequest> createEnergyMeterStandardAlertRules(Device device) {
        List<CreateAlertRuleRequest> ruleRequests = new ArrayList<>();
//        ruleRequests.add(new CreateAlertRuleRequest(device.id(), BATTERY_LEVEL, LESS_THAN, 15f, SeverityLevel.INFO, true, device.))
        return null;
    }

    private List<CreateAlertRuleRequest> createDoorSensorStandardAlertRules(Device device) {
        return null;
    }
}
