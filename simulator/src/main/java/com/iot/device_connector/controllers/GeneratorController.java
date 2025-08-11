package com.iot.device_connector.controllers;

import com.iot.device_connector.generator.TelemetryGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/devices-simulator")
public class GeneratorController {

    private final TelemetryGenerator generator;

    @GetMapping("/rpm/{rpm}")
    public ResponseEntity<String> rpm(@PathVariable int rpm) {
        generator.startWithRpm(rpm);
        return ResponseEntity.ok("Set creation of telemetries to " + rpm + " rpm");
    }

    @GetMapping("/stop")
    public ResponseEntity<String> rpm() {
        generator.shutdown();
        return ResponseEntity.ok("Stopping creation of telemetries...");
    }
}
