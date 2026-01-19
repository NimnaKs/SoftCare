package me.nimnakse.water_management.fixed_assets.consumptions.service;

import java.util.List;
import me.nimnakse.water_management.fixed_assets.consumptions.dto.request.FixedAssetConsumptionCreateReq;
import me.nimnakse.water_management.fixed_assets.consumptions.dto.request.FixedAssetConsumptionUpdateReq;
import me.nimnakse.water_management.fixed_assets.consumptions.dto.response.FixedAssetConsumptionBatchRes;
import me.nimnakse.water_management.fixed_assets.consumptions.dto.response.FixedAssetConsumptionRes;

public interface FixedAssetConsumptionService {
    FixedAssetConsumptionRes create(FixedAssetConsumptionCreateReq request);

    FixedAssetConsumptionRes update(Long id, FixedAssetConsumptionUpdateReq request);

    FixedAssetConsumptionRes getById(Long id);

    List<FixedAssetConsumptionRes> list(Long fixedAssetTemplateId);

    List<FixedAssetConsumptionBatchRes> listAvailableBatches(Long fixedAssetTemplateId);

    void delete(Long id);
}
