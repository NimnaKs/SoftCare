package me.nimnakse.water_management.employees.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record EmployeeCreateReq(
        @NotNull Long orgUnitId,
        @NotBlank String name,
        @NotBlank String nic,
        @NotNull LocalDate dateOfBirth,
        LocalDate dateOfAppointment,
        @NotBlank String mobileNumber,
        String secondaryContactNumber,
        String address,
        @NotBlank String designation,
        String profilePhotoUrl
) {
}
