package com.iot.device_connector.commands;

import com.iot.commands.SmartLightCommand;
import com.iot.device_connector.devices.DevicesProvider;
import com.iot.device_connector.devices.dto.SmartLightDto;
import com.iot.device_connector.kafka.TelemetriesKafkaProducerRunner;
import com.iot.device_connector.model.enums.DeviceType;
import com.iot.devices.DeviceStatus;
import com.iot.devices.SmartLight;
import com.iot.devices.SmartLightMode;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.iot.device_connector.model.enums.DeviceType.SMART_LIGHT;
import static java.util.Optional.ofNullable;

@Slf4j
@Component
public class SmartLightCommandExecutor extends AbstractCommandExecutor<SmartLightCommand, SmartLight, SmartLightDto> {

    public SmartLightCommandExecutor(DevicesProvider devicesProvider, TelemetriesKafkaProducerRunner kafkaProducerRunner) {
        super(devicesProvider, kafkaProducerRunner);
    }

    @Override
    SmartLight applyCommand(SmartLightCommand command, SmartLight device) {
        ofNullable(command.getIsOn()).ifPresent(device::setIsOn);
        ofNullable(command.getBrightness()).ifPresent(device::setBrightness);
        ofNullable(command.getColor()).ifPresent(device::setColor);
        ofNullable(command.getMode()).ifPresent(mode -> device.setMode(SmartLightMode.valueOf(mode.toString())));
        ofNullable(command.getCreatedAt()).ifPresent(device::setLastUpdated);
        Future<RecordMetadata> future = sendMessage(device.getDeviceId(), device);
        try {
            future.get();
            updateDevicesCache(device);
        } catch (Exception e) {
            log.error("Failed to apply command: {}", command, e);
            throw new RuntimeException(e);
        }
        return device;
    }

    @Override
    boolean verifyIfShouldCancel(SmartLightCommand oldCommand, SmartLightCommand newCommand) {
        return false;
    }

    @Override
    DeviceType getDeviceType() {
        return SMART_LIGHT;
    }

    @Override
    SmartLight mapDeviceFromDtoToAvro(SmartLightDto device) {
        return SmartLight.newBuilder()
                .setDeviceId(device.deviceId().toString())
                .setIsOn(device.isOn())
                .setBrightness(device.getBrightness())
                .setColor(device.colour())
                .setMode(ofNullable(device.mode())
                        .map(SmartLightMode::valueOf)
                        .orElse(null))
                .setPowerConsumption(device.getPowerConsumption())
                .setStatus(DeviceStatus.valueOf(device.getStatus().name()))
                .setFirmwareVersion(device.firmwareVersion())
                .setLastUpdated(device.getLastUpdated())
                .build();
    }

    @Override
    SmartLightDto mapDeviceFromAvroToDto(SmartLight device) {
        return null;
    }

    @Override
    Class<SmartLightDto> getClazz() {
        return SmartLightDto.class;
    }
}

