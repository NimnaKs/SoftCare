package me.nimnakse.water_management.organization.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import me.nimnakse.water_management.organization.entity.OrgUnitLevel;

public record OrgUnitUpdateReq(
        @NotBlank String name,
        @NotNull OrgUnitLevel level,
        Long parentId,
        Long waterProjectId,
        String organizationCode
) {
}
