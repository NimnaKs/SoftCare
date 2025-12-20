package me.nimnakse.water_management.employees.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import me.nimnakse.water_management.employees.entity.EmployeeStatus;

public record EmployeeRes(
        Long id,
        Long orgUnitId,
        String name,
        String nic,
        LocalDate dateOfBirth,
        LocalDate dateOfAppointment,
        String mobileNumber,
        String secondaryContactNumber,
        String address,
        String designation,
        String profilePhotoUrl,
        EmployeeStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
