package com.iot.device_connector.generator.alerts;


import jakarta.validation.constraints.NotEmpty;
import lombok.NonNull;

import java.util.Set;
import java.util.UUID;

public record CreateAlertRuleRequest(
        @NotEmpty Set<UUID> deviceIds,
        @NonNull
        MetricType metricType,
        @NonNull
        ThresholdType thresholdType,
        Float thresholdValue,
        @NonNull
        SeverityLevel severity,
        @NonNull
        Boolean isEnabled) {
}
