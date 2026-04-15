package me.nimnakse.water_management.meter_reader_assignments.dto.response;

import java.time.LocalDate;

public record MeterReaderAssignmentRes(
        Long id,
        Long orgUnitId,
        Long readerUserId,
        String readerName,
        String readerUsername,
        Long billingZoneId,
        String billingZoneName,
        LocalDate assignedFrom,
        LocalDate assignedTo,
        String note,
        boolean active
) {
}
