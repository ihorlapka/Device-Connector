package com.iot.device_connector.devices;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.device_connector.devices.dto.DeviceDto;
import com.iot.device_connector.devices.dto.SmartLightDto;
import com.iot.device_connector.devices.dto.SmartPlugDto;
import com.iot.device_connector.devices.dto.ThermostatDto;
import com.iot.device_connector.model.Device;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.iot.device_connector.generator.creators.SmartLightCreator.*;
import static com.iot.device_connector.generator.creators.SmartPlugCreator.*;
import static com.iot.device_connector.generator.creators.ThermostatCreator.*;
import static java.util.Optional.ofNullable;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelemetryParser {

    private final ObjectMapper objectMapper = new ObjectMapper();


    public DeviceDto parse(Device device) {
        return switch (device.deviceType()) {
            case SMART_PLUG -> parseSmartPlug(device);
            case SMART_LIGHT -> parseSmartLight(device);
            case THERMOSTAT -> parseThermostat(device);
            default -> throw new RuntimeException("Parsing telemetry for " + device.deviceType() + " is not implemented!");
        };
    }

    private SmartPlugDto parseSmartPlug(Device device) {
        final SmartPlugDto deviceDto = SmartPlugDto.of(device.id(), device.status(), device.firmwareVersion(),
                device.updatedAt().toInstant(), device.deviceType());
        return parse(device.telemetry(), deviceDto, this::parseAndSetSmartPlugTelemetry);
    }

    private SmartLightDto parseSmartLight(Device device) {
        final SmartLightDto deviceDto = SmartLightDto.of(device.id(), device.status(), device.firmwareVersion(),
                device.updatedAt().toInstant(), device.deviceType());
        return parse(device.telemetry(), deviceDto, this::parseAndSetSmartLightTelemetry);
    }

    private ThermostatDto parseThermostat(Device device) {
        final ThermostatDto deviceDto = ThermostatDto.of(device.id(), device.status(), device.firmwareVersion(),
                device.updatedAt().toInstant(), device.deviceType());
        return parse(device.telemetry(), deviceDto, this::parseAndSetThermostatTelemetry);
    }

    private <T extends DeviceDto> T parse(String telemetry, T dto, BiConsumer<JsonNode, T> parseTelemetry) {
        try {
            final JsonNode jsonNode = objectMapper.readTree(telemetry);
            if (jsonNode == null) {
                return dto;
            }
            parseTelemetry.accept(jsonNode, dto);
            return dto;
        } catch (Exception e) {
            log.warn("Unable to parse telemetry: {}, deviceId={}", telemetry, dto.getDeviceId());
            throw new RuntimeException(e);
        }
    }

    private void parseAndSetSmartPlugTelemetry(JsonNode jsonNode, SmartPlugDto dto) {
        parseField(jsonNode, "isOn", JsonNode::asBoolean, SMART_PLUG_IS_ON_DEFAULT).ifPresent(dto::setIsOn);
        parseField(jsonNode, "voltage", JsonNode::floatValue, SMART_PLUG_VOLTAGE_DEFAULT).ifPresent(dto::setVoltage);
        parseField(jsonNode, "current", JsonNode::floatValue, SMART_PLUG_CURRENT_DEFAULT).ifPresent(dto::setCurrent);
        parseField(jsonNode, "powerUsage", JsonNode::floatValue, SMART_PLUG_POWER_USAGE_DEFAULT).ifPresent(dto::setPowerUsage);
    }

    private void parseAndSetSmartLightTelemetry(JsonNode jsonNode, SmartLightDto dto) {
        parseField(jsonNode, "isOn", JsonNode::asBoolean, SMART_LIGHT_IS_ON_DEFAULT).ifPresent(dto::setIsOn);
        parseField(jsonNode, "brightness", JsonNode::intValue, SMART_LIGHT_BRIGHTNESS_DEFAULT).ifPresent(dto::setBrightness);
        parseField(jsonNode, "colour", JsonNode::textValue, SMART_LIGHT_COLOUR_DEFAULT).ifPresent(dto::setColour);
        parseField(jsonNode, "mode", JsonNode::textValue, SMART_LIGHT_MODE_DEFAULT.name()).ifPresent(dto::setMode);
        parseField(jsonNode, "powerConsumption", JsonNode::floatValue, SMART_LIGHT_POWER_CONSUMPTION_DEFAULT).ifPresent(dto::setPowerConsumption);
    }

    private void parseAndSetThermostatTelemetry(JsonNode jsonNode, ThermostatDto dto) {
        parseField(jsonNode, "currentTemperature", JsonNode::floatValue, THERMOSTAT_CURRENT_TEMPERATURE_DEFAULT).ifPresent(dto::setCurrentTemperature);
        parseField(jsonNode, "targetTemperature", JsonNode::floatValue, THERMOSTAT_TARGET_TEMPERATURE_DEFAULT).ifPresent(dto::setTargetTemperature);
        parseField(jsonNode, "humidity", JsonNode::floatValue, THERMOSTAT_HUMIDITY_DEFAULT).ifPresent(dto::setHumidity);
        parseField(jsonNode, "mode", JsonNode::textValue, THERMOSTAT_DEFAULT_MODE.name()).ifPresent(mode -> dto.setMode(ThermostatMode.valueOf(mode)));
    }

    private <T> Optional<T> parseField(JsonNode jsonNode, String fieldName, Function<JsonNode, T> parseValue, T defaultValue) {
        return jsonNode.hasNonNull(fieldName) ? ofNullable(parseValue.apply(jsonNode.get(fieldName))) : ofNullable(defaultValue);
    }
}
