package me.nimnakse.water_management.fixed_assets.initial_stocks.service;

import java.util.List;
import me.nimnakse.water_management.fixed_assets.initial_stocks.dto.request.FixedAssetInitialStockCreateReq;
import me.nimnakse.water_management.fixed_assets.initial_stocks.dto.request.FixedAssetInitialStockUpdateReq;
import me.nimnakse.water_management.fixed_assets.initial_stocks.dto.response.FixedAssetInitialStockRes;

public interface FixedAssetInitialStockService {
    FixedAssetInitialStockRes create(FixedAssetInitialStockCreateReq request);

    FixedAssetInitialStockRes update(Long id, FixedAssetInitialStockUpdateReq request);

    FixedAssetInitialStockRes getById(Long id);

    List<FixedAssetInitialStockRes> list(Long orgUnitId, Long templateId);

    void delete(Long id);
}
