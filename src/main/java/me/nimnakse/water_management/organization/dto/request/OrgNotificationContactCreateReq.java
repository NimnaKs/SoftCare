package me.nimnakse.water_management.organization.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import me.nimnakse.water_management.common.util.ValidationPatterns;
import jakarta.validation.constraints.NotNull;

public record OrgNotificationContactCreateReq(
        @NotBlank String name,
        @NotBlank
        @Pattern(regexp = ValidationPatterns.SRI_LANKA_MOBILE_REGEX,
                message = "Mobile number must be a 10-digit number starting with 07")
        String mobileNumber,
        @NotNull Integer priorityOrder
) {
}
