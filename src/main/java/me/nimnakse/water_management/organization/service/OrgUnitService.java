package me.nimnakse.water_management.organization.service;

import java.util.List;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.organization.dto.request.OrgUnitCreateReq;
import me.nimnakse.water_management.organization.dto.request.OrgUnitUpdateReq;
import me.nimnakse.water_management.organization.dto.response.OrgUnitRes;
import me.nimnakse.water_management.organization.dto.response.OrgUnitTreeRes;

public interface OrgUnitService {
    OrgUnitRes create(OrgUnitCreateReq request);

    List<OrgUnitTreeRes> getTree(Long waterProjectId);

    OrgUnitRes getById(Long id);

    OrgUnitRes update(Long id, OrgUnitUpdateReq request);

    PageResponse<OrgUnitRes> getAll(int page, int size);

    PageResponse<OrgUnitRes> getLevelOneUnits(int page, int size);

    PageResponse<OrgUnitRes> getLevelTwoUnits(Long levelOneId, int page, int size);

    PageResponse<OrgUnitRes> getLevelThreeUnits(Long levelTwoId, int page, int size);

    PageResponse<OrgUnitRes> getLevelFourUnits(Long levelThreeId, int page, int size);

    PageResponse<OrgUnitRes> getLevelFiveUnits(Long levelFourId, int page, int size);

    void activate(Long id);

    void deactivate(Long id);
}
