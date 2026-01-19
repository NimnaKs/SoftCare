package me.nimnakse.water_management.inventory.consumptions.service;

import java.util.List;
import me.nimnakse.water_management.inventory.consumptions.dto.request.InventoryConsumptionCreateReq;
import me.nimnakse.water_management.inventory.consumptions.dto.request.InventoryConsumptionUpdateReq;
import me.nimnakse.water_management.inventory.consumptions.dto.response.InventoryConsumptionRes;

public interface InventoryConsumptionService {
    InventoryConsumptionRes create(InventoryConsumptionCreateReq request);

    InventoryConsumptionRes update(Long id, InventoryConsumptionUpdateReq request);

    InventoryConsumptionRes getById(Long id);

    List<InventoryConsumptionRes> list(Long inventoryTemplateId);

    void delete(Long id);
}
