package me.nimnakse.water_management.members.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import me.nimnakse.water_management.members.entity.MemberType;

public record MemberUpdateReq(
        @NotBlank String membershipCode,
        @NotNull Long orgUnitId,
        @NotNull MemberType membershipType,
        String salutation,
        String fullName,
        String corporateName,
        String nicNumber,
        @NotBlank String mobileNumber,
        String dpNicFrontUrl,
        String dpNicRearUrl,
        String signatureUrl,
        String brcDocumentUrl
) {
}
