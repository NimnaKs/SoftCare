package me.nimnakse.water_management.connections.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import me.nimnakse.water_management.common.util.ValidationPatterns;

public record ConnectionCreateReq(
        @NotNull Long memberId,
        @NotNull Long premisesId,
        @NotNull Long billingZoneId,
        @NotBlank String accountNumber,
        Long line1Id,
        Long line2Id,
        Long line3Id,
        Long line4Id,
        String houseNumber,
        String houseName,
        String houseNickname,
        Long gnDivisionId,
        Long valveId,
        Long societyId,
        Long clusterId,
        @NotBlank
        @Pattern(regexp = ValidationPatterns.SRI_LANKA_MOBILE_REGEX,
                message = "Mobile number must be a 10-digit number starting with 07")
        String mobileNumber,
        @Pattern(regexp = ValidationPatterns.OPTIONAL_SRI_LANKA_PHONE_REGEX,
                message = "Secondary contact number must be a 10-digit Sri Lankan phone number")
        String secondaryNumber,
        @Pattern(regexp = ValidationPatterns.OPTIONAL_SRI_LANKA_PHONE_REGEX,
                message = "Fixed line number must be a 10-digit Sri Lankan phone number")
        String fixedLineNumber,
        @NotNull Long tariffId
) {
}
