package me.nimnakse.water_management.inventory.master_categories.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InventoryMasterCategoryCreateReq(
        Long parentId,
        @NotNull @Min(0) Integer level,
        @NotBlank @Size(max = 255) String name,
        @Size(max = 255) String specification01,
        @Size(max = 255) String specification02,
        @Size(max = 50) String unit,
        Boolean isLeaf,
        Boolean isSystem,
        Boolean isActive
) {
}
