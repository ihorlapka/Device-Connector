package com.iot.device_connector.commands;

import com.iot.commands.SmartLightCommand;
import com.iot.commands.SmartPlugCommand;
import com.iot.commands.ThermostatCommand;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommandsScheduler {

    private final SmartLightCommandExecutor smartLightCommandExecutor;
    private final SmartPlugCommandExecutor smartPlugCommandExecutor;
    private final ThermostatCommandExecutor thermostatCommandExecutor;


    public void scheduleExecution(SpecificRecord commandRecord) {
        switch (commandRecord) {
            case SmartLightCommand command -> smartLightCommandExecutor.scheduleExecution(command, SmartLightCommand::getDeviceId);
            case SmartPlugCommand command -> smartPlugCommandExecutor.scheduleExecution(command, SmartPlugCommand::getDeviceId);
            case ThermostatCommand command -> thermostatCommandExecutor.scheduleExecution(command, ThermostatCommand::getDeviceId);
            default -> throw new IllegalStateException("Not implemented: " + commandRecord);
        }
    }
}
