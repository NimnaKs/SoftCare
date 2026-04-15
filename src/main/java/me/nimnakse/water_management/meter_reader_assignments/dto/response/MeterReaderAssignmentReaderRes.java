package me.nimnakse.water_management.meter_reader_assignments.dto.response;

public record MeterReaderAssignmentReaderRes(
        Long id,
        String name,
        String username,
        Long orgUnitId
) {
}
