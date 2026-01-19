package me.nimnakse.water_management.inventory.bin_cards.service;

import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.stock_cards.dto.response.BinCardEntryRes;

public interface InventoryBinCardService {
    PageResponse<BinCardEntryRes> getBinCard(Long inventoryTemplateId, int page, int size);
}
