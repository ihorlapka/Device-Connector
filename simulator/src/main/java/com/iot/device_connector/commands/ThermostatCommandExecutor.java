package com.iot.device_connector.commands;

import com.iot.commands.ThermostatCommand;
import com.iot.device_connector.kafka.TelemetriesKafkaProducerRunner;
import com.iot.devices.Thermostat;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static java.util.Optional.ofNullable;

@Slf4j
@Component
public class ThermostatCommandExecutor extends AbstractCommandExecutor<ThermostatCommand, Thermostat> {

    private static final long TIME_PERIOD = 1; //seconds
    private static final BigDecimal STEP = new BigDecimal("0.1");

    public ThermostatCommandExecutor(TelemetriesKafkaProducerRunner kafkaProducerRunner) {
        super(kafkaProducerRunner);
    }

    @Override
    Thermostat applyCommand(ThermostatCommand command, Thermostat device) {
        ofNullable(command.getTargetTemperature()).ifPresent(device::setTargetTemperature);
        ofNullable(command.getCreatedAt()).ifPresent(device::setLastUpdated);

        final BigDecimal target = BigDecimal.valueOf(device.getTargetTemperature()).setScale(1, RoundingMode.HALF_UP);
        BigDecimal current = BigDecimal.valueOf(device.getCurrentTemperature()).setScale(1, RoundingMode.HALF_UP);

        while (target.compareTo(current) != 0) {
            try {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Command cancelled");
                }
                device.setCurrentTemperature(current.floatValue());
                Future<RecordMetadata> future = sendMessage(device.getDeviceId(), device);
                RecordMetadata metadata = future.get(1, TimeUnit.SECONDS);
                log.info("Sent during command execution: {}, offset={}", device, metadata.offset());
                sleep(Duration.ofSeconds(TIME_PERIOD));
                current = target.compareTo(current) > 0 ? current.add(STEP) : current.subtract(STEP);
            } catch (InterruptedException e) {
                try {
                    log.info("New command cancelled current executing one {}, device state: {}", command, device);
                    Future<RecordMetadata> future = sendMessage(device.getDeviceId(), device);
                    future.get(1, TimeUnit.SECONDS);
                    log.info("Sent after command execution was cancelled: {}", device);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                } finally {
                    Thread.currentThread().interrupt();
                }
            } catch (Exception e) {
                log.error("Unexpected exception during command execution, {} {}", command, device);
                throw new RuntimeException(e);
            }
        }
        return device;
    }

    @Override
    boolean verifyIfShouldCancel(ThermostatCommand oldCommand, ThermostatCommand newCommand) {
        return !Objects.equals(newCommand.getTargetTemperature(), oldCommand.getTargetTemperature());
    }
}
