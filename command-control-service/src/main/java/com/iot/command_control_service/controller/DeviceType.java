package com.iot.command_control_service.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeviceType {
    THERMOSTAT("Thermostat"),
    DOOR_SENSOR("DoorSensor"),
    SMART_LIGHT("SmartLight"),
    ENERGY_METER("EnergyMeter"),
    SMART_PLUG("SmartPlug"),
    TEMPERATURE_SENSOR("TemperatureSensor"),
    SOIL_MOISTURE_SENSOR("SoilMoistureSensor");

    private final String id;
}
