package me.nimnakse.water_management.fixed_assets.templates.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FixedAssetTemplateCreateReq(
        @NotNull(message = "Level 1 category id is required") Long levelOneCategoryId,
        @NotNull(message = "Level 2 category id is required") Long levelTwoCategoryId,
        @NotNull(message = "Level 3 category id is required") Long levelThreeCategoryId,
        @Size(max = 50, message = "Template code cannot exceed 50 characters") String templateCode
) {
}
