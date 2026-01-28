package com.iot.command_control_service.kafka;

import com.iot.devices.Command;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.kafka.KafkaClientMetrics;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Future;

@Slf4j
@Component
public class KafkaProducerRunner {

    private final KafkaProducerProperties kafkaProducerProperties;
    private final KafkaProducer<String, Command> kafkaProducer;
    private final KafkaClientMetrics kafkaClientMetrics;


    public KafkaProducerRunner(MeterRegistry meterRegistry, KafkaProducerProperties kafkaProducerProperties) {
        this.kafkaProducerProperties = kafkaProducerProperties;
        this.kafkaProducer = new KafkaProducer<>(getProperties(kafkaProducerProperties.getProperties()));
        this.kafkaClientMetrics = new KafkaClientMetrics(kafkaProducer);
        this.kafkaClientMetrics.bindTo(meterRegistry);
    }

    public Future<RecordMetadata> send(UUID deviceId, Command event) {
        log.info("Sending to topic={}, deviceId={}, message={}", kafkaProducerProperties.getTopic(), deviceId, event);
        final ProducerRecord<String, Command> record = new ProducerRecord<>(kafkaProducerProperties.getTopic(), deviceId.toString(), event);
        return kafkaProducer.send(record, getCallback(event));
    }

    private Callback getCallback(Command message) {
        return (metadata, exception) -> {
            if (exception != null) {
                log.error("Failed to send record to topic={}, message={}, error={}",
                        kafkaProducerProperties.getTopic(), message, exception.getMessage(), exception);
            } else {
                log.debug("Successfully sent record to topic={}, partition={}, offset={}",
                        kafkaProducerProperties.getTopic(), metadata.partition(), metadata.offset());
            }
        };
    }

    private Properties getProperties(Map<String, String> producerProperties) {
        Properties properties = new Properties();
        properties.putAll(producerProperties);
        return properties;
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        log.info("Shutting down KafkaProducer...");
        if (kafkaProducer != null) {
            try {
                kafkaProducer.flush();
                kafkaProducer.close(Duration.ofMillis(kafkaProducerProperties.getExecutorTerminationTimeoutMs()));
                log.info("KafkaProducer closed successfully.");
            } catch (Exception e) {
                log.warn("Exception during KafkaProducer shutdown: {}", e.getMessage(), e);
            }
        }
        if (kafkaClientMetrics != null) {
            kafkaClientMetrics.close();
            log.info("KafkaClientMetrics are closed");
        }
    }
}
