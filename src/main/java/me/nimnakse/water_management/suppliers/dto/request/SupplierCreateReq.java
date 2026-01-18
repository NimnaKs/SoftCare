package me.nimnakse.water_management.suppliers.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import me.nimnakse.water_management.common.util.ValidationPatterns;

public record SupplierCreateReq(
        @NotNull Long orgUnitId,
        @NotBlank String name,
        String address,
        String brcNumber,
        String nic,
        String mobileNumber1,
        String mobileNumber2,
        @Pattern(regexp = ValidationPatterns.OPTIONAL_SRI_LANKA_PHONE_REGEX,
                message = "Telephone number must be a 10-digit Sri Lankan phone number")
        String telephoneNumber,
        @Email(message = "Email must be valid")
        String email
) {
}
