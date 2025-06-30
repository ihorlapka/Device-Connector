package com.iot.device_connector;

import org.springframework.boot.SpringApplication;

public class TestDeviceConnectorApplication {

	public static void main(String[] args) {
		SpringApplication.from(DeviceConnectorApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
