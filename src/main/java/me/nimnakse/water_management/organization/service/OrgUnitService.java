package me.nimnakse.water_management.organization.service;

import java.util.List;
import me.nimnakse.water_management.organization.dto.request.OrgUnitCreateReq;
import me.nimnakse.water_management.organization.dto.request.OrgUnitUpdateReq;
import me.nimnakse.water_management.organization.dto.response.OrgUnitRes;
import me.nimnakse.water_management.organization.dto.response.OrgUnitTreeRes;

public interface OrgUnitService {
    OrgUnitRes create(OrgUnitCreateReq request);

    List<OrgUnitTreeRes> getTree(Long waterProjectId);

    OrgUnitRes getById(Long id);

    OrgUnitRes update(Long id, OrgUnitUpdateReq request);
}
