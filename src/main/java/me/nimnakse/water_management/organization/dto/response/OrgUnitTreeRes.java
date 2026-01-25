package me.nimnakse.water_management.organization.dto.response;

import java.util.List;
import me.nimnakse.water_management.organization.entity.OrgUnitLevel;

public record OrgUnitTreeRes(
                Long id,
                String name,
                OrgUnitLevel level,
                Long parentId,
                Long waterProjectId,
                String organizationCode,
                Boolean isActive,
                List<OrgUnitTreeRes> children) {
}
