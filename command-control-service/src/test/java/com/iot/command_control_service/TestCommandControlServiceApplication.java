package com.iot.command_control_service;

import org.springframework.boot.SpringApplication;

public class TestCommandControlServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(CommandControlServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
