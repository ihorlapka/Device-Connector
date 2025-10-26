package com.iot.device_connector.generator.alerts;

import java.util.UUID;

public record AlertRuleResponse(UUID ruleId,
                                UUID deviceId,
                                MetricType metricType,
                                ThresholdType thresholdType,
                                Float thresholdValue,
                                SeverityLevel severity,
                                boolean isEnabled) {
}
