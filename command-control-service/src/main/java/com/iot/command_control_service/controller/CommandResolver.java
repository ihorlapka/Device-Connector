package com.iot.command_control_service.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.commands.SmartLightCommand;
import com.iot.commands.SmartLightMode;
import com.iot.commands.SmartPlugCommand;
import com.iot.commands.ThermostatCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandResolver {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public SpecificRecord resolve(CommandRequest request) {
        return switch (request.deviceType()) {
            case SMART_LIGHT -> mapSmartLightCommand(request);
            case SMART_PLUG -> mapSmartPlugCommand(request);
            case THERMOSTAT -> mapThermostatCommand(request);
            default -> throw new RuntimeException("Command resolver is not implemented for " + request.deviceType());
        };
    }

    private SpecificRecord mapSmartLightCommand(CommandRequest request) {
        final SmartLightCommand.Builder commandBuilder = SmartLightCommand.newBuilder()
                .setCommandId(UUID.randomUUID().toString())
                .setDeviceId(request.deviceId().toString())
                .setCreatedAt(Instant.now());
        parseAndSetFields(request.payload(), commandBuilder, request.deviceId(), this::parseAndSetSmartLightTelemetry);
        return commandBuilder.build();
    }

    private SpecificRecord mapSmartPlugCommand(CommandRequest request) {
        final SmartPlugCommand.Builder commandBuilder = SmartPlugCommand.newBuilder()
                .setCommandId(UUID.randomUUID().toString())
                .setDeviceId(request.deviceId().toString())
                .setCreatedAt(Instant.now());
        parseAndSetFields(request.payload(), commandBuilder, request.deviceId(), this::parseAndSetSmartPlugTelemetry);
        return commandBuilder.build();
    }

    private SpecificRecord mapThermostatCommand(CommandRequest request) {
        final ThermostatCommand.Builder commandBuilder = ThermostatCommand.newBuilder()
                .setCommandId(UUID.randomUUID().toString())
                .setDeviceId(request.deviceId().toString())
                .setCreatedAt(Instant.now());
        parseAndSetFields(request.payload(), commandBuilder, request.deviceId(), this::parseAndSetThermostatTelemetry);
        return commandBuilder.build();
    }

    private <B> void parseAndSetFields(String commandPayload, B builder, UUID deviceId, BiConsumer<JsonNode, B> setField) {
        try {
            final JsonNode jsonNode = objectMapper.readTree(commandPayload);
            if (jsonNode == null) {
                return;
            }
            setField.accept(jsonNode, builder);
        } catch (Exception e) {
            log.warn("Unable to parse command payload: {}, deviceId={}", commandPayload, deviceId);
            throw new RuntimeException(e);
        }
    }

    private <T> Optional<T> parseField(JsonNode jsonNode, String fieldName, Function<JsonNode, T> parseValue) {
        return jsonNode.hasNonNull(fieldName) ? ofNullable(parseValue.apply(jsonNode.get(fieldName))) : empty();
    }

    private void parseAndSetSmartLightTelemetry(JsonNode jsonNode, SmartLightCommand.Builder builder) {
        parseField(jsonNode, "isOn", JsonNode::asBoolean).ifPresent(builder::setIsOn);
        parseField(jsonNode, "brightness", JsonNode::intValue).ifPresent(builder::setBrightness);
        parseField(jsonNode, "colour", JsonNode::textValue).ifPresent(builder::setColor);
        parseField(jsonNode, "mode", JsonNode::textValue).ifPresent(SmartLightMode::valueOf);
    }

    private void parseAndSetSmartPlugTelemetry(JsonNode jsonNode, SmartPlugCommand.Builder builder) {
        parseField(jsonNode, "isOn", JsonNode::asBoolean).ifPresent(builder::setIsOn);
    }

    private void parseAndSetThermostatTelemetry(JsonNode jsonNode, ThermostatCommand.Builder builder) {
        parseField(jsonNode, "targetTemperature", JsonNode::floatValue).ifPresent(builder::setTargetTemperature);
    }
}
