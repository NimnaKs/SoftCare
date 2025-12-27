package me.nimnakse.water_management.inventory.master_categories.service;

import java.util.List;
import me.nimnakse.water_management.inventory.master_categories.dto.request.InventoryMasterCategoryCreateReq;
import me.nimnakse.water_management.inventory.master_categories.dto.request.InventoryMasterCategoryUpdateReq;
import me.nimnakse.water_management.inventory.master_categories.dto.response.InventoryMasterCategoryRes;
import me.nimnakse.water_management.inventory.master_categories.dto.response.InventoryMasterCategoryTreeRes;

public interface InventoryMasterCategoryService {
    InventoryMasterCategoryRes create(InventoryMasterCategoryCreateReq request);

    InventoryMasterCategoryRes update(Long id, InventoryMasterCategoryUpdateReq request);

    InventoryMasterCategoryRes getById(Long id);

    List<InventoryMasterCategoryRes> list();

    List<InventoryMasterCategoryTreeRes> getTree();

    void delete(Long id);
}
