package com.iot.device_connector.kafka;

import com.iot.devices.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
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

//    @PostConstruct
    public void sendMessage(String key, SpecificRecord record) {

//        TemperatureSensor record = TemperatureSensor.newBuilder()
//                .setDeviceId("dd4ed12c-2d4d-4c8f-ac7b-0ceb2d180ab7")
//                .setLastUpdated(Instant.now())
//                .setStatus(DeviceStatus.ONLINE)
//                .setPressure(6f)
//                .build();

//        String timestamp = "2025-08-05T09:38:41.322+00:00";
//        Instant instant = OffsetDateTime.parse(timestamp).toInstant();

//        SoilMoistureSensor record = SoilMoistureSensor.newBuilder()
//                .setDeviceId("0a6eb124-8e3a-4569-98c4-cdb16a2476e2")
//                .setLastUpdated(Instant.now())
//                .setStatus(DeviceStatus.ONLINE)
//                .setBatteryLevel(65)
//                .setMoisturePercentage(14f)
//                .setFirmwareVersion("1.0.13")
//                .setDoorState(DoorState.CLOSED)
//                .build();

//        log.info("\n" + SmartLight.SCHEMA$.toString());
//        log.info("\n" + SmartPlug.SCHEMA$.toString());
//        log.info("\n" + SoilMoistureSensor.SCHEMA$.toString());
//        log.info("\n" + Thermostat.SCHEMA$.toString());

        try {
            final ProducerRecord<String, SpecificRecord> producerRecord = new ProducerRecord<>(producerProperties.getTopic(), key, record);
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
