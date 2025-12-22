package me.nimnakse.water_management.organization.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import me.nimnakse.water_management.common.util.ValidationPatterns;

public record AuthorizedOfficerUpdateReq(
        @NotBlank String designation,
        @NotBlank String name,
        @NotBlank String nic,
        @NotBlank
        @Pattern(regexp = ValidationPatterns.SRI_LANKA_MOBILE_REGEX,
                message = "Mobile number must be a 10-digit number starting with 07")
        String mobileNumber,
        String stampPhotoUrl,
        String signaturePhotoUrl,
        boolean active
) {
}
