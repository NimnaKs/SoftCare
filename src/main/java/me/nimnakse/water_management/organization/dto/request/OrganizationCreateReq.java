package me.nimnakse.water_management.organization.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import me.nimnakse.water_management.common.util.ValidationPatterns;

public record OrganizationCreateReq(
        @NotNull Long orgUnitId,
        @NotBlank String nameEn,
        String nameSi,
        String nameTa,
        @NotBlank String addressEn,
        String addressSi,
        String addressTa,
        @NotBlank String postalCode,
        String registrationNumber,
        @Email(message = "Email must be valid")
        String email,
        @NotBlank
        @Pattern(regexp = ValidationPatterns.SRI_LANKA_MOBILE_REGEX,
                message = "Mobile number must be a 10-digit number starting with 07")
        String mobileNumber,
        @Pattern(regexp = ValidationPatterns.OPTIONAL_SRI_LANKA_PHONE_REGEX,
                message = "Telephone number must be a 10-digit Sri Lankan phone number")
        String telephoneNumber,
        String logoUrl
) {
}
