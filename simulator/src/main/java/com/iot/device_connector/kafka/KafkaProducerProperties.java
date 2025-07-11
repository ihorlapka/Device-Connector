package com.iot.device_connector.kafka;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@Getter
@Setter
@ToString
@Configuration
@ConfigurationProperties(KafkaProducerProperties.PROPERTIES_PREFIX)
@RequiredArgsConstructor
public class KafkaProducerProperties {

    final static String PROPERTIES_PREFIX = "kafka.producer";

    private Map<String, String> properties = new HashMap<>();

    @Value("${" + PROPERTIES_PREFIX + ".topic}")
    private String topic;

    @PostConstruct
    private void logProperties() {
        log.info("kafka producer properties: {}", this);
    }
}
