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
@ConfigurationProperties(KafkaConsumerProperties.PROPERTIES_PREFIX)
@RequiredArgsConstructor
public class KafkaConsumerProperties {

    final static String PROPERTIES_PREFIX = "kafka.consumer";

    private Map<String, Object> properties = new HashMap<>();

    @Value("${" + PROPERTIES_PREFIX + ".dead-letter-topic}")
    private String deadLetterTopic;

    @Value("${" + PROPERTIES_PREFIX + ".poll-timeout-ms}")
    private Long pollTimeoutMs;

    @Value("${" + PROPERTIES_PREFIX + ".retries}")
    private Long retries;

    @Value("${" + PROPERTIES_PREFIX + ".retry-interval-ms}")
    private Long retryIntervalMs;

    @PostConstruct
    private void logProperties() {
        log.info("kafka consumer properties: {}", this);
    }
}
