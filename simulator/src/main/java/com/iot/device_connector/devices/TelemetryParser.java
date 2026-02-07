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

import static java.util.Optional.empty;
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
        parseField(jsonNode, "isOn", JsonNode::asBoolean).ifPresent(dto::setIsOn);
        parseField(jsonNode, "voltage", JsonNode::floatValue).ifPresent(dto::setVoltage);
        parseField(jsonNode, "current", JsonNode::floatValue).ifPresent(dto::setCurrent);
        parseField(jsonNode, "powerUsage", JsonNode::floatValue).ifPresent(dto::setPowerUsage);
    }

    private void parseAndSetSmartLightTelemetry(JsonNode jsonNode, SmartLightDto dto) {
        parseField(jsonNode, "isOn", JsonNode::asBoolean).ifPresent(dto::setIsOn);
        parseField(jsonNode, "brightness", JsonNode::intValue).ifPresent(dto::setBrightness);
        parseField(jsonNode, "colour", JsonNode::textValue).ifPresent(dto::setColour);
        parseField(jsonNode, "mode", JsonNode::textValue).ifPresent(dto::setMode);
        parseField(jsonNode, "powerConsumption", JsonNode::floatValue).ifPresent(dto::setPowerConsumption);
    }

    private void parseAndSetThermostatTelemetry(JsonNode jsonNode, ThermostatDto dto) {
        parseField(jsonNode, "currentTemperature", JsonNode::floatValue).ifPresent(dto::setCurrentTemperature);
        parseField(jsonNode, "targetTemperature", JsonNode::floatValue).ifPresent(dto::setTargetTemperature);
        parseField(jsonNode, "humidity", JsonNode::floatValue).ifPresent(dto::setHumidity);
        parseField(jsonNode, "mode", JsonNode::textValue).ifPresent(mode -> dto.setMode(ThermostatMode.valueOf(mode)));
    }

    private <T> Optional<T> parseField(JsonNode jsonNode, String fieldName, Function<JsonNode, T> parseValue) {
        return jsonNode.hasNonNull(fieldName) ? ofNullable(parseValue.apply(jsonNode.get(fieldName))) : empty();
    }
}
