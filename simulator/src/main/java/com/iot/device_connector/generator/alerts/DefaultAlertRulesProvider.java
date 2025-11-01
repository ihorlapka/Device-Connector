package com.iot.device_connector.generator.alerts;

import com.iot.device_connector.model.enums.DeviceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.iot.device_connector.generator.alerts.MetricType.*;
import static com.iot.device_connector.generator.alerts.SeverityLevel.*;
import static com.iot.device_connector.generator.alerts.ThresholdType.*;
import static java.util.Collections.emptySet;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultAlertRulesProvider {

    public Set<AlertRule> getAlertRules(DeviceType deviceType) {
        return switch (deviceType) {
            case DOOR_SENSOR -> getDoorSensorAlertRules();
            case ENERGY_METER -> getEnergyMeterAlertRules();
            case SMART_LIGHT -> getSmartLightAlertRules();
            case SMART_PLUG -> getSmartPlugAlertRules();
            case SOIL_MOISTURE_SENSOR -> getSoilMoistureSensorAlertRules();
            case TEMPERATURE_SENSOR, THERMOSTAT -> getTemperatureAdHumidityAlertRules();
            default -> {
                log.info("No default alert rules present for deviceType: {}", deviceType);
                yield emptySet();
            }
        };
    }

    private Set<AlertRule> getDoorSensorAlertRules() {
        return Set.of(
                new AlertRule(null, BATTERY_LEVEL, LESS_THAN, 15.f, INFO, true),
                new AlertRule(null, BATTERY_LEVEL, LESS_THAN, 5.f, WARNING, true),
                new AlertRule(null, BATTERY_LEVEL, LESS_THAN, 1.f, CRITICAL, true),
                new AlertRule(null, TAMPER, EQUAL_TO, null, CRITICAL, true),
                new AlertRule(null, TIME_OUT, GREATER_THAN, 300.f, INFO, true),
                new AlertRule(null, TIME_OUT, GREATER_THAN, 600.f, WARNING, true),
                new AlertRule(null, TIME_OUT, GREATER_THAN, 900.f, CRITICAL, true)
        );
    }

    private Set<AlertRule> getEnergyMeterAlertRules() {
        float infoMinVoltage = 215f;
        float warningMinVoltage = 195f;
        float criticalMinVoltage = 180f;
        float infoMaxVoltage = 235f;
        float warningMaxVoltage = 250f;
        float criticalMaxVoltage = 275f;
        float infoMinCurrent = 5f;
        float warningMinCurrent = 4f;
        float criticalMinCurrent = 3f;
        float infoMaxCurrent = 10f;
        float warningMaxCurrent = 20f;
        float criticalMaxWarning = 60f;
        return Set.of(
                new AlertRule(null, VOLTAGE, LESS_THAN, infoMinVoltage, INFO, true),
                new AlertRule(null, VOLTAGE, LESS_THAN, warningMinVoltage, WARNING, true),
                new AlertRule(null, VOLTAGE, LESS_THAN, criticalMinVoltage, CRITICAL, true),
                new AlertRule(null, VOLTAGE, GREATER_THAN, infoMaxVoltage, INFO, true),
                new AlertRule(null, VOLTAGE, GREATER_THAN, warningMaxVoltage, WARNING, true),
                new AlertRule(null, VOLTAGE, GREATER_THAN, criticalMaxVoltage, CRITICAL, true),
                new AlertRule(null, CURRENT, LESS_THAN, infoMinCurrent, INFO, true),
                new AlertRule(null, CURRENT, LESS_THAN, warningMinCurrent, WARNING, true),
                new AlertRule(null, CURRENT, LESS_THAN, criticalMinCurrent, CRITICAL, true),
                new AlertRule(null, CURRENT, GREATER_THAN, infoMaxCurrent, INFO, true),
                new AlertRule(null, CURRENT, GREATER_THAN, warningMaxCurrent, WARNING, true),
                new AlertRule(null, CURRENT, GREATER_THAN, criticalMaxWarning, CRITICAL, true),
                new AlertRule(null, POWER, LESS_THAN, infoMinCurrent * infoMinVoltage, INFO, true),
                new AlertRule(null, POWER, LESS_THAN, warningMinCurrent * warningMinVoltage, WARNING, true),
                new AlertRule(null, POWER, LESS_THAN, criticalMinCurrent * criticalMinVoltage, CRITICAL, true),
                new AlertRule(null, POWER, GREATER_THAN, infoMaxCurrent * infoMaxVoltage, INFO, true),
                new AlertRule(null, POWER, GREATER_THAN, warningMaxCurrent * warningMaxVoltage, WARNING, true),
                new AlertRule(null, POWER, GREATER_THAN, criticalMaxWarning * criticalMaxVoltage, CRITICAL, true),
                new AlertRule(null, ENERGY_CONSUMED, GREATER_THAN, infoMaxCurrent * infoMaxVoltage, CRITICAL, true),
                new AlertRule(null, ENERGY_CONSUMED, GREATER_THAN, warningMaxCurrent * warningMaxVoltage, CRITICAL, true),
                new AlertRule(null, ENERGY_CONSUMED, GREATER_THAN, criticalMaxWarning * criticalMaxVoltage, CRITICAL, true)
        );
    }

    private Set<AlertRule> getSmartLightAlertRules() {
        return Set.of(
                new AlertRule(null, ENERGY_CONSUMED, GREATER_THAN, 12f, CRITICAL, true),
                new AlertRule(null, ENERGY_CONSUMED, GREATER_THAN, 18f, CRITICAL, true),
                new AlertRule(null, ENERGY_CONSUMED, GREATER_THAN, 30f, CRITICAL, true)
        );
    }

    private Set<AlertRule> getSmartPlugAlertRules() {
        float infoMinVoltage = 215f;
        float warningMinVoltage = 195f;
        float criticalMinVoltage = 180f;
        float infoMaxVoltage = 235f;
        float warningMaxVoltage = 250f;
        float criticalMaxVoltage = 275f;
        float infoMinCurrent = 5f;
        float warningMinCurrent = 4f;
        float criticalMinCurrent = 3f;
        float infoMaxCurrent = 10f;
        float warningMaxCurrent = 20f;
        float criticalMaxWarning = 60f;
        return Set.of(
                new AlertRule(null, VOLTAGE, LESS_THAN, infoMinVoltage, INFO, true),
                new AlertRule(null, VOLTAGE, LESS_THAN, warningMinVoltage, WARNING, true),
                new AlertRule(null, VOLTAGE, LESS_THAN, criticalMinVoltage, CRITICAL, true),
                new AlertRule(null, VOLTAGE, GREATER_THAN, infoMaxVoltage, INFO, true),
                new AlertRule(null, VOLTAGE, GREATER_THAN, warningMaxVoltage, WARNING, true),
                new AlertRule(null, VOLTAGE, GREATER_THAN, criticalMaxVoltage, CRITICAL, true),
                new AlertRule(null, CURRENT, LESS_THAN, infoMinCurrent, INFO, true),
                new AlertRule(null, CURRENT, LESS_THAN, warningMinCurrent, WARNING, true),
                new AlertRule(null, CURRENT, LESS_THAN, criticalMinCurrent, CRITICAL, true),
                new AlertRule(null, CURRENT, GREATER_THAN, infoMaxCurrent, INFO, true),
                new AlertRule(null, CURRENT, GREATER_THAN, warningMaxCurrent, WARNING, true),
                new AlertRule(null, CURRENT, GREATER_THAN, criticalMaxWarning, CRITICAL, true),
                new AlertRule(null, POWER, LESS_THAN, infoMinCurrent * infoMinVoltage, INFO, true),
                new AlertRule(null, POWER, LESS_THAN, warningMinCurrent * warningMinVoltage, WARNING, true),
                new AlertRule(null, POWER, LESS_THAN, criticalMinCurrent * criticalMinVoltage, CRITICAL, true),
                new AlertRule(null, POWER, GREATER_THAN, infoMaxCurrent * infoMaxVoltage, INFO, true),
                new AlertRule(null, POWER, GREATER_THAN, warningMaxCurrent * warningMaxVoltage, WARNING, true),
                new AlertRule(null, POWER, GREATER_THAN, criticalMaxWarning * criticalMaxVoltage, CRITICAL, true)
        );
    }

    private Set<AlertRule> getSoilMoistureSensorAlertRules() {
        return Set.of(
                new AlertRule(null, PERCENTAGE, GREATER_THAN, 5.f, INFO, true),
                new AlertRule(null, PERCENTAGE, GREATER_THAN, 10.f, WARNING, true),
                new AlertRule(null, PERCENTAGE, GREATER_THAN, 15.f, CRITICAL, true),
                new AlertRule(null, BATTERY_LEVEL, LESS_THAN, 15.f, INFO, true),
                new AlertRule(null, BATTERY_LEVEL, LESS_THAN, 5.f, WARNING, true),
                new AlertRule(null, BATTERY_LEVEL, LESS_THAN, 1.f, CRITICAL, true),
                new AlertRule(null, TEMPERATURE, GREATER_THAN, 25.f, INFO, true),
                new AlertRule(null, TEMPERATURE, GREATER_THAN, 30.f, WARNING, true),
                new AlertRule(null, TEMPERATURE, GREATER_THAN, 35.f, CRITICAL, true)
        );
    }

    private Set<AlertRule> getTemperatureAdHumidityAlertRules() {
        return Set.of(
                new AlertRule(null, TEMPERATURE, GREATER_THAN, 25.f, INFO, true),
                new AlertRule(null, TEMPERATURE, GREATER_THAN, 30.f, WARNING, true),
                new AlertRule(null, TEMPERATURE, GREATER_THAN, 35.f, CRITICAL, true),
                new AlertRule(null, TEMPERATURE, LESS_THAN, 20.f, INFO, true),
                new AlertRule(null, TEMPERATURE, LESS_THAN, 16.f, WARNING, true),
                new AlertRule(null, TEMPERATURE, LESS_THAN, 12.f, CRITICAL, true),
                new AlertRule(null, HUMIDITY, GREATER_THAN, 60.f, INFO, true),
                new AlertRule(null, HUMIDITY, GREATER_THAN, 65.f, WARNING, true),
                new AlertRule(null, HUMIDITY, GREATER_THAN, 70.f, CRITICAL, true),
                new AlertRule(null, HUMIDITY, LESS_THAN, 40.f, INFO, true),
                new AlertRule(null, HUMIDITY, LESS_THAN, 35.f, WARNING, true),
                new AlertRule(null, HUMIDITY, LESS_THAN, 30.f, CRITICAL, true)
        );
    }
}
