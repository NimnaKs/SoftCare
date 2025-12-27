package me.nimnakse.water_management.fixed_assets.master_categories.service;

import java.util.List;
import me.nimnakse.water_management.fixed_assets.master_categories.dto.request.FixedAssetMasterCategoryCreateReq;
import me.nimnakse.water_management.fixed_assets.master_categories.dto.request.FixedAssetMasterCategoryUpdateReq;
import me.nimnakse.water_management.fixed_assets.master_categories.dto.response.FixedAssetMasterCategoryRes;

public interface FixedAssetMasterCategoryService {
    FixedAssetMasterCategoryRes create(FixedAssetMasterCategoryCreateReq request);

    FixedAssetMasterCategoryRes update(Long id, FixedAssetMasterCategoryUpdateReq request);

    FixedAssetMasterCategoryRes getById(Long id);

    List<FixedAssetMasterCategoryRes> list(Long parentId);

    void delete(Long id);
}
