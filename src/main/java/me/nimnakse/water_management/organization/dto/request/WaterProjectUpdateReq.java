package me.nimnakse.water_management.organization.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import me.nimnakse.water_management.organization.entity.WaterProjectStatus;

public record WaterProjectUpdateReq(
        @NotBlank String name,
        @NotNull WaterProjectStatus status,
        String departmentOrgName,
        OffsetDateTime registeredAt
) {
}
