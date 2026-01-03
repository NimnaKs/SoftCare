package me.nimnakse.water_management.fixed_assets.templates.service;

import java.util.List;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.fixed_assets.templates.dto.request.FixedAssetTemplateCreateReq;
import me.nimnakse.water_management.fixed_assets.templates.dto.request.FixedAssetTemplateImportReq;
import me.nimnakse.water_management.fixed_assets.templates.dto.request.FixedAssetTemplateUpdateReq;
import me.nimnakse.water_management.fixed_assets.templates.dto.response.FixedAssetTemplateRes;

public interface FixedAssetTemplateService {
    FixedAssetTemplateRes create(FixedAssetTemplateCreateReq request);

    FixedAssetTemplateRes update(Long id, FixedAssetTemplateUpdateReq request);

    FixedAssetTemplateRes getById(Long id);

    PageResponse<FixedAssetTemplateRes> getPage(int page, int size);

    void createIfMissing(Long levelOneCategoryId, Long levelTwoCategoryId, Long levelThreeCategoryId);

    void delete(Long id);

    List<FixedAssetTemplateRes> importTemplates(FixedAssetTemplateImportReq request);

    PageResponse<FixedAssetTemplateRes> getByOrgUnit(Long orgUnitId, int page, int size);
}
