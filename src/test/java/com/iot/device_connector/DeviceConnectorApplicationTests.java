package com.iot.device_connector;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class DeviceConnectorApplicationTests {

	@Test
	void contextLoads() {
	}

}
