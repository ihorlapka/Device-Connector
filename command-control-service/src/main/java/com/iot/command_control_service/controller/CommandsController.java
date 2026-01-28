package com.iot.command_control_service.controller;

import com.iot.command_control_service.controller.errors.CommandNotSentException;
import com.iot.command_control_service.controller.errors.PermissionDeniedException;
import com.iot.command_control_service.kafka.KafkaProducerRunner;
import com.iot.command_control_service.services.RegistryServiceClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Future;
import com.iot.devices.Command;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CommandsController {

    private final RegistryServiceClient registryClient;
    private final KafkaProducerRunner kafkaProducer;

    @PostMapping
    public ResponseEntity<?> sendCommand(@RequestBody CommandRequest commandRequest, HttpServletRequest httpServletRequest) {
        log.info("Received: {}", commandRequest);
        if (registryClient.checkAccess(commandRequest, httpServletRequest)) {
            try {

                final Command command = mapToAvro(commandRequest);
                final Future<RecordMetadata> future = kafkaProducer.send(commandRequest.deviceId(), command);
                final RecordMetadata metadata = future.get();
                log.info("Command {} is successfully sent, offset={}", command, metadata.offset());
                return ResponseEntity.ok().body(mapToDto(command));
            } catch (Exception e) {
                throw new CommandNotSentException(commandRequest.deviceId(), commandRequest.userId(), e);
            }
        } else {
            throw new PermissionDeniedException(commandRequest.userId());
        }
    }

    private Command mapToAvro(CommandRequest request) {
        return new Command(UUID.randomUUID().toString(), request.deviceId().toString(),
                request.userId().toString(), request.payload(), Instant.now());
    }

    private static CommandDto mapToDto(Command command) {
        return new CommandDto(command.getCommandId(), command.getDeviceId(),
                command.getUserId(), command.getPayload(), command.getCreatedAt());
    }
}
