package me.nimnakse.water_management.inventory.master_categories.service;

import java.util.List;
import me.nimnakse.water_management.inventory.master_categories.dto.request.InventoryMasterCategoryCreateReq;
import me.nimnakse.water_management.inventory.master_categories.dto.request.InventoryMasterCategoryUpdateReq;
import me.nimnakse.water_management.inventory.master_categories.dto.response.InventoryMasterCategoryRes;

public interface InventoryMasterCategoryService {
    InventoryMasterCategoryRes create(InventoryMasterCategoryCreateReq request);

    InventoryMasterCategoryRes update(Long id, InventoryMasterCategoryUpdateReq request);

    InventoryMasterCategoryRes getById(Long id);

    List<InventoryMasterCategoryRes> list();

    void delete(Long id);
}
