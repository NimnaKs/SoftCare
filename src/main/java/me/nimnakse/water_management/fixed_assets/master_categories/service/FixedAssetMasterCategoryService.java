package me.nimnakse.water_management.fixed_assets.master_categories.service;

import java.util.List;
import me.nimnakse.water_management.fixed_assets.master_categories.dto.request.FixedAssetMasterCategoryCreateReq;
import me.nimnakse.water_management.fixed_assets.master_categories.dto.request.FixedAssetMasterCategoryUpdateReq;
import me.nimnakse.water_management.fixed_assets.master_categories.dto.response.FixedAssetMasterCategoryRes;
import me.nimnakse.water_management.fixed_assets.master_categories.dto.response.FixedAssetMasterCategoryTreeRes;
import me.nimnakse.water_management.common.api.PageResponse;

public interface FixedAssetMasterCategoryService {
    FixedAssetMasterCategoryRes create(FixedAssetMasterCategoryCreateReq request);

    FixedAssetMasterCategoryRes update(Long id, FixedAssetMasterCategoryUpdateReq request);

    FixedAssetMasterCategoryRes getById(Long id);

    List<FixedAssetMasterCategoryRes> list(Long parentId);

    List<FixedAssetMasterCategoryTreeRes> getTree();

    PageResponse<FixedAssetMasterCategoryRes> getLevelOneCategories(int page, int size);

    PageResponse<FixedAssetMasterCategoryRes> getLevelTwoCategories(Long levelOneCategoryId, int page, int size);

    PageResponse<FixedAssetMasterCategoryRes> getLevelThreeCategories(Long levelTwoCategoryId, int page, int size);

    void delete(Long id);
}
