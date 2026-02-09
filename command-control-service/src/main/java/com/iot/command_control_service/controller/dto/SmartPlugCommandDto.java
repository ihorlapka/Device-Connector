package com.iot.command_control_service.controller.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@ToString
@RequiredArgsConstructor(staticName = "of")
public class SmartPlugCommandDto implements CommandDto {
    private final String commandId;
    private final String deviceId;
    private final Boolean isOn;
    private final Instant createdAt;
}
