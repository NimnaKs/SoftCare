package me.nimnakse.water_management.inventory.master_categories.service;

import java.util.List;
import me.nimnakse.water_management.inventory.master_categories.dto.request.InventoryMasterCategoryCreateReq;
import me.nimnakse.water_management.inventory.master_categories.dto.request.InventoryMasterCategoryUpdateReq;
import me.nimnakse.water_management.inventory.master_categories.dto.response.InventoryMasterCategoryRes;
import me.nimnakse.water_management.inventory.master_categories.dto.response.InventoryMasterCategoryTreeRes;
import me.nimnakse.water_management.common.api.PageResponse;

public interface InventoryMasterCategoryService {
    InventoryMasterCategoryRes create(InventoryMasterCategoryCreateReq request);

    InventoryMasterCategoryRes update(Long id, InventoryMasterCategoryUpdateReq request);

    InventoryMasterCategoryRes getById(Long id);

    List<InventoryMasterCategoryRes> list();

    List<InventoryMasterCategoryTreeRes> getTree();

    PageResponse<InventoryMasterCategoryRes> getLevelOneCategories(int page, int size);

    PageResponse<InventoryMasterCategoryRes> getLevelTwoCategories(Long levelOneCategoryId, int page, int size);

    PageResponse<InventoryMasterCategoryRes> getLevelThreeCategories(Long levelTwoCategoryId, int page, int size);

    void delete(Long id);
}
