package me.nimnakse.water_management.inventory.templates.service;

import java.util.List;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.inventory.templates.dto.request.InventoryTemplateCreateReq;
import me.nimnakse.water_management.inventory.templates.dto.request.InventoryTemplateImportReq;
import me.nimnakse.water_management.inventory.templates.dto.request.InventoryTemplateUpdateReq;
import me.nimnakse.water_management.inventory.templates.dto.response.InventoryTemplateRes;

public interface InventoryTemplateService {
    InventoryTemplateRes create(InventoryTemplateCreateReq request);

    InventoryTemplateRes update(Long id, InventoryTemplateUpdateReq request);

    InventoryTemplateRes getById(Long id);

    PageResponse<InventoryTemplateRes> getPage(int page, int size);

    void createIfMissing(Long levelOneCategoryId, Long levelTwoCategoryId, Long levelThreeCategoryId);

    void delete(Long id);

    List<InventoryTemplateRes> importTemplates(InventoryTemplateImportReq request);

    PageResponse<InventoryTemplateRes> getByOrgUnit(Long orgUnitId, int page, int size);
}
