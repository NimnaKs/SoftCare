package me.nimnakse.water_management.fixed_assets.bin_cards.service;

import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.stock_cards.dto.response.BinCardEntryRes;

public interface FixedAssetBinCardService {
    PageResponse<BinCardEntryRes> getBinCard(Long fixedAssetTemplateId, int page, int size);
}
