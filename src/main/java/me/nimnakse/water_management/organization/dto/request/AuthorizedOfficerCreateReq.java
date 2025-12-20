package me.nimnakse.water_management.organization.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AuthorizedOfficerCreateReq(
        @NotBlank String designation,
        @NotBlank String name,
        @NotBlank String nic,
        @NotBlank String mobileNumber,
        String stampPhotoUrl,
        String signaturePhotoUrl
) {
}
