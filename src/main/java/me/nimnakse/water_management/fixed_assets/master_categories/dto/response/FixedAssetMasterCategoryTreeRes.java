package me.nimnakse.water_management.fixed_assets.master_categories.dto.response;

import java.util.List;

public record FixedAssetMasterCategoryTreeRes(
        Long id,
        Long parentId,
        Integer level,
        String name,
        String specification01,
        String specification02,
        String unit,
        Boolean isSystem,
        Boolean isActive,
        List<FixedAssetMasterCategoryTreeRes> children
) {
}
