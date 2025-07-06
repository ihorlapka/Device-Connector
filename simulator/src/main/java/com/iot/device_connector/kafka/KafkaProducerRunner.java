package com.iot.device_connector.kafka;

import com.iot.devices.DeviceStatus;
import com.iot.devices.TempUnit;
import com.iot.devices.TemperatureSensor;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
@Component
public class KafkaProducerRunner {

    private final KafkaProducerProperties producerProperties;
    private final KafkaProducer<String, SpecificRecord> kafkaProducer;


    public KafkaProducerRunner(KafkaProducerProperties producerProperties) {
        Properties properties = new Properties();
        properties.putAll(producerProperties.getProperties());
        this.kafkaProducer = new KafkaProducer<>(properties);
        this.producerProperties = producerProperties;
    }

    @PostConstruct
    public void sendMessage() {

        TemperatureSensor record = TemperatureSensor.newBuilder()
                .setDeviceId("dd4ed12c-2d4d-4c8f-ac7b-0ceb2d180ab7")
                .setFirmwareVersion("1.0.6v")
                .setLastUpdated(Instant.now())
                .setUnit(TempUnit.K)
                .setStatus(DeviceStatus.ONLINE)
//                .setTemperature(31f)
//                .setPressure(10f)
                .setHumidity(14.54f)
                .build();

        try {
            final ProducerRecord<String, SpecificRecord> producerRecord = new ProducerRecord<>(producerProperties.getTopic(), record.getDeviceId(), record);
            final Future<RecordMetadata> future = kafkaProducer.send(producerRecord);
            final RecordMetadata recordMetadata = future.get();
            if (recordMetadata.hasOffset()) {
                log.info("Message is successfully sent {}", record);
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to send message: {}", record, e);
            throw new RuntimeException("Unable to send message!");
        }

    }
}
