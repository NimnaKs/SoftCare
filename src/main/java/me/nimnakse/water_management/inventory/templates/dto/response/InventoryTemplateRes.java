package me.nimnakse.water_management.inventory.templates.dto.response;

import java.time.Instant;

public record InventoryTemplateRes(
        Long id,
        String templateCode,
        Long levelOneCategoryId,
        String levelOneCategoryName,
        Long levelTwoCategoryId,
        String levelTwoCategoryName,
        Long levelThreeCategoryId,
        String levelThreeCategoryName,
        Instant createdAt,
        Instant updatedAt
) {
}
