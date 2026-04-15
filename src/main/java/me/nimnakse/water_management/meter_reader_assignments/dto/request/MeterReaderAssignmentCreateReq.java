package me.nimnakse.water_management.meter_reader_assignments.dto.request;

import java.time.LocalDate;
import java.util.List;

public record MeterReaderAssignmentCreateReq(
        Long orgUnitId,
        Long readerUserId,
        List<Long> billingZoneIds,
        LocalDate assignedFrom,
        LocalDate assignedTo,
        String note
) {
}
