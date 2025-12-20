package me.nimnakse.water_management.organization.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrgNotificationContactUpdateReq(
        @NotBlank String name,
        @NotBlank String mobileNumber,
        @NotNull Integer priorityOrder
) {
}
