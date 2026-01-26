package com.iot.device_connector.generator;

import com.iot.device_connector.auth.AuthenticationResponse;
import com.iot.device_connector.kafka.TelemetriesKafkaProducerRunner;
import com.iot.device_connector.model.Device;
import com.iot.device_connector.model.User;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class AlertRulesGenerator extends AbstractGenerator {

    private final AlertingRulesCreator alertingRulesCreator;

    public AlertRulesGenerator(RestTemplate restTemplate, TelemetriesKafkaProducerRunner kafkaProducerRunner,
                               TelemetryCreator telemetryCreator, AlertingRulesCreator alertingRulesCreator) {
        super(restTemplate, kafkaProducerRunner, telemetryCreator);
        this.alertingRulesCreator = alertingRulesCreator;
    }

    public void generate() {
        final AuthenticationResponse authResponse = login();
        final List<Device> devices = loadDevices(authResponse, "",
                new ParameterizedTypeReference<List<User>>() {}, getDevicesFromManyUsersFunction());
        logout(authResponse);
        alertingRulesCreator.create(devices, authResponse);
    }

    @Override
    String getLoadingUri() {
        return USERS_URL;
    }
}
