package com.iot.device_connector.config;

import com.iot.device_connector.kafka.KafkaConsumerProperties;
import com.iot.devices.Command;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@EnableKafka
@Configuration
public class KafkaConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Command> kafkaListenerContainerFactory(ConsumerFactory<String, Command> consumerFactory,
                                                                                                  DeadLetterPublishingRecoverer deadLetterPublishingRecoverer,
                                                                                                  KafkaConsumerProperties props) {
        ConcurrentKafkaListenerContainerFactory<String, Command> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1);
        factory.setCommonErrorHandler(new DefaultErrorHandler(deadLetterPublishingRecoverer, new FixedBackOff(props.getRetryIntervalMs(), props.getRetries())));
        factory.getContainerProperties().setPollTimeout(props.getPollTimeoutMs());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    @Bean
    public DeadLetterPublishingRecoverer recoverer(KafkaTemplate<String, Command> template,
                                                   KafkaConsumerProperties props) {
        return new DeadLetterPublishingRecoverer(template,
                (record, exception) -> {
                    log.error("Sending to dead letter topic {}, due to exception:", record.value(), exception);
                    return new TopicPartition(props.getDeadLetterTopic(), record.partition());
                });
    }

    @Bean
    public ConsumerFactory<String, Command> consumerFactory(KafkaConsumerProperties props) {
        Map<String, Object> properties = new HashMap<>(props.getProperties().size());
        properties.putAll(props.getProperties());
        return new DefaultKafkaConsumerFactory<>(properties);
    }
}
