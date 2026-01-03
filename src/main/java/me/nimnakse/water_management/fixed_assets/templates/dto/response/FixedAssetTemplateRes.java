package me.nimnakse.water_management.fixed_assets.templates.dto.response;

import java.time.Instant;

public record FixedAssetTemplateRes(
        Long id,
        String templateCode,
        Long levelOneCategoryId,
        String levelOneCategoryName,
        Long levelTwoCategoryId,
        String levelTwoCategoryName,
        String levelTwoCategorySpecification01,
        String levelTwoCategorySpecification02,
        Long levelThreeCategoryId,
        String levelThreeCategoryName,
        Instant createdAt,
        Instant updatedAt
) {
}
