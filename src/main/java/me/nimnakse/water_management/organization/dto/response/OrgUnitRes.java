package me.nimnakse.water_management.organization.dto.response;

import me.nimnakse.water_management.organization.entity.OrgUnitLevel;

public record OrgUnitRes(
        Long id,
        String name,
        OrgUnitLevel level,
        Long parentId,
        Long waterProjectId,
        String organizationCode
) {
}
