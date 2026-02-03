package com.iot.device_connector.kafka;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.Future;

@Slf4j
@Component
public class TelemetriesKafkaProducerRunner {

    private final TelemetriesKafkaProducerProperties producerProperties;
    private final KafkaProducer<String, SpecificRecord> kafkaProducer;


    public TelemetriesKafkaProducerRunner(TelemetriesKafkaProducerProperties producerProperties) {
        Properties properties = new Properties();
        properties.putAll(producerProperties.getProperties());
        this.kafkaProducer = new KafkaProducer<>(properties);
        this.producerProperties = producerProperties;
    }

    public Future<RecordMetadata> sendMessage(String key, SpecificRecord record) {
        try {
            final ProducerRecord<String, SpecificRecord> producerRecord = new ProducerRecord<>(producerProperties.getTopic(), key, record);
            return kafkaProducer.send(producerRecord, getCallback(record));
        } catch (Exception e) {
            log.error("Failed to send message: {}", record, e);
            throw new RuntimeException(e);
        }
    }

    private Callback getCallback(SpecificRecord telemetry) {
        return (metadata, exception) -> {
            if (exception != null) {
                log.error("Failed to send telemetry to topic={}, telemetry={}, error={}", metadata.topic(), telemetry, exception.getMessage(), exception);
            } else {
                log.debug("Successfully sent telemetry to topic={}, partition={}, offset={}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
        };
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down KafkaProducer...");
        if (kafkaProducer != null) {
            try {
                kafkaProducer.flush();
                kafkaProducer.close(Duration.ofSeconds(10));
                log.info("KafkaProducer closed successfully.");
            } catch (Exception e) {
                log.warn("Exception during KafkaProducer shutdown: {}", e.getMessage(), e);
            }
        }
    }
}
