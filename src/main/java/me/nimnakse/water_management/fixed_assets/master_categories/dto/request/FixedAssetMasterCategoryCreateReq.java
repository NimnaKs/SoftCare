package me.nimnakse.water_management.fixed_assets.master_categories.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FixedAssetMasterCategoryCreateReq(
        Long parentId,
        @NotNull Integer level,
        @NotBlank String name,
        String specification01,
        String specification02,
        String unit,
        Boolean isLeaf,
        Boolean isSystem,
        Boolean isActive
) {
}
