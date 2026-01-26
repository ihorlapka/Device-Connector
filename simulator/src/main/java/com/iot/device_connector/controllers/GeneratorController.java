package com.iot.device_connector.controllers;

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
    private final UserTelemetriesGenerator userTelemetriesGenerator;

    @GetMapping("/rpm/{rpm}")
    public ResponseEntity<String> generate(@PathVariable int rpm) {
        generator.startWithRpm(rpm);
        return ResponseEntity.ok("Set creation of telemetries to " + rpm + " rpm");
    }

    @GetMapping("/username/{username}/rpm/{rpm}")
    public ResponseEntity<String> generateForUser(@PathVariable String username, @PathVariable int rpm) {
        userTelemetriesGenerator.startWithRpmForUser(username, rpm);
        return ResponseEntity.ok("Set creation of telemetries to " + rpm + " rpm for user: " + username);
    }

    @GetMapping("/stop")
    public ResponseEntity<String> stop() {
        generator.shutdown();
        return ResponseEntity.ok("Stopping creation of telemetries...");
    }

    @GetMapping("/stopForUser")
    public ResponseEntity<String> stopForUser() {
        userTelemetriesGenerator.shutdown();
        return ResponseEntity.ok("Stopping creation of telemetries for user...");
    }
}
