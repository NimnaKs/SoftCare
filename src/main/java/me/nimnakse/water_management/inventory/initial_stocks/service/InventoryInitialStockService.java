package me.nimnakse.water_management.inventory.initial_stocks.service;

import java.util.List;
import me.nimnakse.water_management.inventory.initial_stocks.dto.request.InventoryInitialStockCreateReq;
import me.nimnakse.water_management.inventory.initial_stocks.dto.request.InventoryInitialStockUpdateReq;
import me.nimnakse.water_management.inventory.initial_stocks.dto.response.InventoryInitialStockRes;

public interface InventoryInitialStockService {
    InventoryInitialStockRes create(InventoryInitialStockCreateReq request);

    InventoryInitialStockRes update(Long id, InventoryInitialStockUpdateReq request);

    InventoryInitialStockRes getById(Long id);

    List<InventoryInitialStockRes> list(Long orgUnitId, Long templateId);

    void delete(Long id);
}
