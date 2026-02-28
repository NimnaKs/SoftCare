package me.nimnakse.water_management.liabilities.main_categories.dto.response;
import java.time.Instant;
import me.nimnakse.water_management.liabilities.LiabilityType;
public record LiabilityMainCategoryRes(
        Long id,
        LiabilityType liabilityType,
        String name,
        String description,
        Boolean isSystem,
        Boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
}
