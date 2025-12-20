package me.nimnakse.water_management.organization.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import me.nimnakse.water_management.organization.entity.WaterProjectStatus;

public record WaterProjectCreateReq(
        @NotBlank String name,
        WaterProjectStatus status,
        String departmentOrgName,
        OffsetDateTime registeredAt
) {
}
