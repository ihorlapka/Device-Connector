package com.iot.device_connector.kafka;

import com.iot.devices.Command;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandsKafkaConsumerListener {

    final static String PROPERTIES_PREFIX = "kafka.consumer";


    @KafkaListener(topics = "${" + PROPERTIES_PREFIX + ".topic}",
            containerFactory = "kafkaListenerContainerFactory")
    public void listen(ConsumerRecord<String, Command> record, Acknowledgment ack) {
        try {
            log.info("Received key: {}, value: {}, partition: {}, offset: {}",
                    record.key(), record.value(), record.partition(), record.offset());

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing message", e);
        }
    }
}
