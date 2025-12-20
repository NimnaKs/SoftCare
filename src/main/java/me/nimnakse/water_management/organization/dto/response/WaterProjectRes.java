package me.nimnakse.water_management.organization.dto.response;

import java.time.OffsetDateTime;
import me.nimnakse.water_management.organization.entity.WaterProjectStatus;

public record WaterProjectRes(
        Long id,
        String name,
        WaterProjectStatus status,
        String departmentOrgName,
        OffsetDateTime registeredAt
) {
}
