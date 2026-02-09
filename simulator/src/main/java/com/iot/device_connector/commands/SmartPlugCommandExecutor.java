package com.iot.device_connector.commands;

import com.iot.commands.SmartPlugCommand;
import com.iot.device_connector.devices.DevicesProvider;
import com.iot.device_connector.devices.dto.SmartPlugDto;
import com.iot.device_connector.kafka.TelemetriesKafkaProducerRunner;
import com.iot.device_connector.model.enums.DeviceType;
import com.iot.devices.DeviceStatus;
import com.iot.devices.SmartPlug;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.Future;

import static com.iot.device_connector.model.enums.DeviceType.SMART_PLUG;
import static java.util.Optional.ofNullable;

@Slf4j
@Component
public class SmartPlugCommandExecutor extends AbstractCommandExecutor<SmartPlugCommand, SmartPlug, SmartPlugDto> {

    public SmartPlugCommandExecutor(DevicesProvider devicesProvider, TelemetriesKafkaProducerRunner kafkaProducerRunner) {
        super(devicesProvider, kafkaProducerRunner);
    }

    @Override
    SmartPlug applyCommand(SmartPlugCommand command, SmartPlug device) {
        ofNullable(command.getIsOn()).ifPresent(device::setIsOn);
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
    boolean verifyIfShouldCancel(SmartPlugCommand oldCommand, SmartPlugCommand newCommand) {
        return false;
    }

    @Override
    DeviceType getDeviceType() {
        return SMART_PLUG;
    }

    @Override
    SmartPlug mapDeviceFromDtoToAvro(SmartPlugDto device) {
        return SmartPlug.newBuilder()
                .setDeviceId(device.getDeviceId().toString())
                .setIsOn(device.getIsOn())
                .setVoltage(device.getVoltage())
                .setCurrent(device.getCurrent())
                .setPowerUsage(device.getPowerUsage())
                .setStatus(DeviceStatus.valueOf(device.getStatus().name()))
                .setFirmwareVersion(device.getFirmwareVersion())
                .setLastUpdated(device.getLastUpdated())
                .build();
    }

    @Override
    SmartPlugDto mapDeviceFromAvroToDto(SmartPlug device) {
        return SmartPlugDto.builder()
                .deviceId(UUID.fromString(device.getDeviceId()))
                .deviceType(SMART_PLUG)
                .isOn(device.getIsOn())
                .voltage(device.getVoltage())
                .current(device.getCurrent())
                .powerUsage(device.getPowerUsage())
                .status(com.iot.device_connector.model.enums.DeviceStatus.valueOf(device.getStatus().name()))
                .firmwareVersion(device.getFirmwareVersion())
                .lastUpdated(device.getLastUpdated())
                .build();
    }
}
