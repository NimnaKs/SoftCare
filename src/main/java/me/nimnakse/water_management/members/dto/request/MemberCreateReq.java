package me.nimnakse.water_management.members.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import me.nimnakse.water_management.common.util.ValidationPatterns;
import me.nimnakse.water_management.members.entity.MemberType;

public record MemberCreateReq(
        @NotNull Long orgUnitId,
        @NotNull MemberType membershipType,
        String salutation,
        String fullName,
        String corporateName,
        String registrationNumber,
        String nicNumber,
        @NotBlank
        @Pattern(regexp = ValidationPatterns.SRI_LANKA_MOBILE_REGEX,
                message = "Mobile number must be a 10-digit number starting with 07")
        String mobileNumber,
        String dpNicFrontUrl,
        String dpNicRearUrl,
        String signatureUrl,
        String brcDocumentUrl
) {
}
