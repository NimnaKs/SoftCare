package me.nimnakse.water_management.employees.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import me.nimnakse.water_management.common.util.ValidationPatterns;

public record EmployeeCreateReq(
        @NotNull Long orgUnitId,
        @NotBlank String name,
        @NotBlank String nic,
        @NotNull LocalDate dateOfBirth,
        LocalDate dateOfAppointment,
        @NotBlank
        @Pattern(regexp = ValidationPatterns.SRI_LANKA_MOBILE_REGEX,
                message = "Mobile number must be a 10-digit number starting with 07")
        String mobileNumber,
        @Pattern(regexp = ValidationPatterns.OPTIONAL_SRI_LANKA_PHONE_REGEX,
                message = "Secondary contact number must be a 10-digit Sri Lankan phone number")
        String secondaryContactNumber,
        String address,
        @NotBlank String designation,
        String profilePhotoUrl
) {
}
