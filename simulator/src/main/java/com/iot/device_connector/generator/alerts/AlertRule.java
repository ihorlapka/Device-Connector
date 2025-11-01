package com.iot.device_connector.generator.alerts;

import java.util.UUID;

public record AlertRule(UUID ruleId,
                        MetricType metricType,
                        ThresholdType thresholdType,
                        Float thresholdValue,
                        SeverityLevel severity,
                        boolean isEnabled) {
}
