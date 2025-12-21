package me.nimnakse.water_management.organization.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrganizationUpdateReq(
        @NotNull Long orgUnitId,
        @NotBlank String nameEn,
        String nameSi,
        String nameTa,
        @NotBlank String addressEn,
        String addressSi,
        String addressTa,
        @NotBlank String postalCode,
        String registrationNumber,
        @Email String email,
        @NotBlank String mobileNumber,
        String telephoneNumber,
        String logoUrl
) {
}
