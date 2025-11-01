package com.iot.device_connector.generator.alerts;


import lombok.NonNull;

import java.util.UUID;

public record CreateAlertRuleRequest(
        @NonNull
        UUID deviceId,
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
