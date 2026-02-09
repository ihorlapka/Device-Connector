package com.iot.command_control_service.controller;

import com.iot.command_control_service.controller.dto.CommandDto;
import com.iot.command_control_service.controller.dto.SmartLightCommandDto;
import com.iot.command_control_service.controller.dto.SmartPlugCommandDto;
import com.iot.command_control_service.controller.dto.ThermostatCommandDto;
import com.iot.commands.SmartLightCommand;
import com.iot.commands.SmartPlugCommand;
import com.iot.commands.ThermostatCommand;
import lombok.experimental.UtilityClass;
import org.apache.avro.specific.SpecificRecord;

@UtilityClass
public class DtoMapper {

    public static CommandDto mapToDto(SpecificRecord record) {
        return switch (record) {
            case SmartLightCommand sl -> mapSmartLightCommand(sl);
            case SmartPlugCommand sl -> mapSmartPlugCommand(sl);
            case ThermostatCommand sl -> mapThermostatCommand(sl);
            default -> throw new RuntimeException("Mapping from command record to dto is not implemented for " + record.getClass().getSimpleName());
        };
    }

    private static CommandDto mapThermostatCommand(ThermostatCommand tc) {
        return ThermostatCommandDto.of(tc.getCommandId(), tc.getDeviceId(), tc.getTargetTemperature(), tc.getCreatedAt());
    }

    private static CommandDto mapSmartPlugCommand(SmartPlugCommand spc) {
        return SmartPlugCommandDto.of(spc.getCommandId(), spc.getDeviceId(), spc.getIsOn(), spc.getCreatedAt());
    }

    private static CommandDto mapSmartLightCommand(SmartLightCommand slc) {
        return SmartLightCommandDto.of(slc.getCommandId(), slc.getDeviceId(), slc.getIsOn(),
                slc.getBrightness(), slc.getColor(), slc.getMode().name(), slc.getCreatedAt());
    }
}
