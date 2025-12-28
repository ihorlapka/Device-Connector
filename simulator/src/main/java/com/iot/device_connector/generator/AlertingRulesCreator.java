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

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.iot.device_connector.generator.TelemetryGenerator.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertingRulesCreator {

    private final RestTemplate restTemplate;
    private final DefaultAlertRulesProvider alertRulesProvider;

    private static final String ALERT_RULES_URL = "/iot-registry/api/v1/alertRules";

    public void create(List<Device> devices, AuthenticationResponse authResponse) {
        for (Device device : devices) {
            final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(HTTP + HOSTNAME + ALERT_RULES_URL);
            List<RequestEntity<CreateAlertRuleRequest>> alertRulesRequests = buildHttpEntities(device, authResponse, builder);

            for (RequestEntity<CreateAlertRuleRequest> alertRuleRequest : alertRulesRequests) {
                try {
                    log.info("Sending create alert rule request for deviceId={}, {}", device.id(), alertRuleRequest);
                    ResponseEntity<AlertRuleResponse> response = restTemplate.postForEntity(builder.toUriString(), alertRuleRequest, AlertRuleResponse.class, Map.of());
                    log.info("Response: {}", response.getBody());
                } catch (Exception e) {
                    log.error("Unexpected exception occurred: {}", alertRuleRequest, e);
                }
            }
        }
    }

    private List<RequestEntity<CreateAlertRuleRequest>> buildHttpEntities(Device device, AuthenticationResponse authResponse,
                                                                          UriComponentsBuilder builder) {
        final MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add(AUTHORIZATION, TOKEN_PREFIX + authResponse.getAccessToken());
        return alertRulesProvider.getAlertRules(device.deviceType()).stream()
                .map(alertRule -> new CreateAlertRuleRequest(Set.of(device.id()), alertRule.metricType(),
                        alertRule.thresholdType(), alertRule.thresholdValue(), alertRule.severity(), true))
                .map(request -> new RequestEntity<>(request, headers, HttpMethod.POST, builder.build(Map.of())))
                .toList();
    }
}
