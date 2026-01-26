package com.iot.device_connector.controllers;

import com.iot.device_connector.generator.AlertRulesGenerator;
import com.iot.device_connector.generator.GenerateTelemetryRequest;
import com.iot.device_connector.generator.TelemetryGenerator;
import com.iot.device_connector.generator.UserTelemetriesGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/devices-simulator")
public class GeneratorController {

    private final TelemetryGenerator generator;
    private final UserTelemetriesGenerator userGenerator;
    private final AlertRulesGenerator rulesGenerator;

    @GetMapping("/rpm/{rpm}")
    public ResponseEntity<String> generateForAllUsers(@PathVariable int rpm) {
        generator.startWithRpm(rpm);
        return ResponseEntity.ok("Set creation of telemetries to " + rpm + " rpm");
    }

    @GetMapping("/generateAlertRules")
    public ResponseEntity<String> generateRules() {
        rulesGenerator.generate();
        return ResponseEntity.ok("Generating standard alert rules for all users' devices");
    }

    @PostMapping
    public ResponseEntity<String> generateForSingleUser(@RequestBody GenerateTelemetryRequest request) {
        userGenerator.startWithRpm(request.username(), request.deviceIds(), request.rpm());
        return ResponseEntity.ok("Set creation of telemetries to " + request.rpm() + " rpm for user: " + request.username());
    }

    @GetMapping("/stop")
    public ResponseEntity<String> stop() {
        generator.stop();
        return ResponseEntity.ok("Stopping creation of telemetries...");
    }

    @GetMapping("/stopForUser")
    public ResponseEntity<String> stopForUser() {
        userGenerator.stop();
        return ResponseEntity.ok("Stopping creation of telemetries for user...");
    }
}
