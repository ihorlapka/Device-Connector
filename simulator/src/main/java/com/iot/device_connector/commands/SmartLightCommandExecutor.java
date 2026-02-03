package com.iot.device_connector.commands;

import com.iot.commands.SmartLightCommand;
import com.iot.device_connector.kafka.TelemetriesKafkaProducerRunner;
import com.iot.devices.SmartLight;
import com.iot.devices.SmartLightMode;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;

@Component
public class SmartLightCommandExecutor extends AbstractCommandExecutor<SmartLightCommand, SmartLight> {

    public SmartLightCommandExecutor(TelemetriesKafkaProducerRunner kafkaProducerRunner) {
        super(kafkaProducerRunner);
    }

    @Override
    SmartLight applyCommand(SmartLightCommand command, SmartLight device) {
        ofNullable(command.getIsOn()).ifPresent(device::setIsOn);
        ofNullable(command.getBrightness()).ifPresent(device::setBrightness);
        ofNullable(command.getColor()).ifPresent(device::setColor);
        ofNullable(command.getMode()).ifPresent(mode -> device.setMode(SmartLightMode.valueOf(mode.toString())));
        ofNullable(command.getCreatedAt()).ifPresent(device::setLastUpdated);
        return device;
    }

    @Override
    boolean verifyIfShouldCancel(SmartLightCommand oldCommand, SmartLightCommand newCommand) {
        return false;
    }

}

