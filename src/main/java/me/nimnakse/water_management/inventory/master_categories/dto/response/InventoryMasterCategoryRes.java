package me.nimnakse.water_management.inventory.master_categories.dto.response;

import java.time.Instant;

public record InventoryMasterCategoryRes(
        Long id,
        Long parentId,
        Integer level,
        String name,
        String specification01,
        String specification02,
        String unit,
        Boolean isLeaf,
        Boolean isSystem,
        Boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
}
