package me.nimnakse.water_management.connections.dto.response;

import java.time.Instant;

public record ConnectionMeterReadingRes(
        String referenceId,
        String meterSerialNumber,
        String updateStatus,
        Instant dateTime,
        Integer reading,
        String createdBy
) {
}
