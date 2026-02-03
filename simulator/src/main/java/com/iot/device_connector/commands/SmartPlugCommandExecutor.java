package com.iot.device_connector.commands;

import com.iot.commands.SmartPlugCommand;
import com.iot.device_connector.kafka.TelemetriesKafkaProducerRunner;
import com.iot.devices.SmartPlug;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;

@Component
public class SmartPlugCommandExecutor extends AbstractCommandExecutor<SmartPlugCommand, SmartPlug> {

    public SmartPlugCommandExecutor(TelemetriesKafkaProducerRunner kafkaProducerRunner) {
        super(kafkaProducerRunner);
    }

    @Override
    SmartPlug applyCommand(SmartPlugCommand command, SmartPlug device) {
        ofNullable(command.getIsOn()).ifPresent(device::setIsOn);
        ofNullable(command.getCreatedAt()).ifPresent(device::setLastUpdated);
        return device;
    }

    @Override
    boolean verifyIfShouldCancel(SmartPlugCommand oldCommand, SmartPlugCommand newCommand) {
        return false;
    }
}
