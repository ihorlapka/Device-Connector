package com.iot.device_connector.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@EnableScheduling
@Configuration
public class RestConfig {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
