package com.iot.device_connector.commands;

import com.iot.commands.ThermostatCommand;
import com.iot.device_connector.devices.DevicesProvider;
import com.iot.device_connector.devices.dto.ThermostatDto;
import com.iot.device_connector.kafka.TelemetriesKafkaProducerRunner;
import com.iot.device_connector.model.enums.DeviceType;
import com.iot.devices.DeviceStatus;
import com.iot.devices.Thermostat;
import com.iot.devices.ThermostatMode;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.iot.device_connector.model.enums.DeviceType.THERMOSTAT;
import static com.iot.devices.ThermostatMode.COOL;
import static com.iot.devices.ThermostatMode.HEAT;
import static java.lang.Thread.sleep;
import static java.util.Optional.ofNullable;

@Slf4j
@Component
public class ThermostatCommandExecutor extends AbstractCommandExecutor<ThermostatCommand, Thermostat, ThermostatDto> {

    private static final long TIME_PERIOD = 1; //seconds
    private static final BigDecimal STEP = new BigDecimal("0.1");

    public ThermostatCommandExecutor(DevicesProvider devicesProvider, TelemetriesKafkaProducerRunner kafkaProducerRunner) {
        super(devicesProvider, kafkaProducerRunner);
    }

    @Override
    Thermostat applyCommand(ThermostatCommand command, Thermostat device) {
        ofNullable(command.getTargetTemperature()).ifPresent(device::setTargetTemperature);

        final BigDecimal target = BigDecimal.valueOf(device.getTargetTemperature()).setScale(1, RoundingMode.HALF_UP);
        BigDecimal current = BigDecimal.valueOf(device.getCurrentTemperature()).setScale(1, RoundingMode.HALF_UP);

        while (target.compareTo(current) != 0) {
            try {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Command cancelled");
                }
                final boolean isHeat = target.compareTo(current) > 0;
                current = isHeat ? current.add(STEP) : current.subtract(STEP);
                device.setCurrentTemperature(current.floatValue());
                device.setMode(isHeat ? HEAT : COOL);
                device.setLastUpdated(Instant.now());
                Future<RecordMetadata> future = sendMessage(device.getDeviceId(), device);
                RecordMetadata metadata = future.get(1, TimeUnit.SECONDS);
                updateDevicesCache(device);
                log.info("Sent during command execution: {}, offset={}", device, metadata.offset());
                sleep(Duration.ofSeconds(TIME_PERIOD));
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

    @Override
    DeviceType getDeviceType() {
        return THERMOSTAT;
    }

    @Override
    Thermostat mapDeviceFromDtoToAvro(ThermostatDto device) {
        return Thermostat.newBuilder()
                .setDeviceId(device.getDeviceId().toString())
                .setCurrentTemperature(device.getCurrentTemperature())
                .setTargetTemperature(device.getTargetTemperature())
                .setHumidity(device.getHumidity())
                .setMode(ofNullable(device.getMode())
                        .map(mode -> ThermostatMode.valueOf(mode.name()))
                        .orElse(null))
                .setStatus(DeviceStatus.valueOf(device.getStatus().name()))
                .setFirmwareVersion(device.getFirmwareVersion())
                .setLastUpdated(device.getLastUpdated())
                .build();
    }

    @Override
    ThermostatDto mapDeviceFromAvroToDto(Thermostat device) {
        return ThermostatDto.builder()
                .deviceId(UUID.fromString(device.getDeviceId()))
                .deviceType(THERMOSTAT)
                .currentTemperature(device.getCurrentTemperature())
                .targetTemperature(device.getTargetTemperature())
                .humidity(device.getHumidity())
                .mode(ofNullable(device.getMode())
                        .map(mode -> com.iot.device_connector.devices.ThermostatMode.valueOf(mode.name()))
                        .orElse(null))
                .status(com.iot.device_connector.model.enums.DeviceStatus.valueOf(device.getStatus().name()))
                .firmwareVersion(device.getFirmwareVersion())
                .lastUpdated(device.getLastUpdated())
                .build();
    }
}
