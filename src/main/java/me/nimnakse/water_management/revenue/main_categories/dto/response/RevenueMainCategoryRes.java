package me.nimnakse.water_management.revenue.main_categories.dto.response;

import java.time.Instant;
import me.nimnakse.water_management.revenue.RevenueCustomerType;

public record RevenueMainCategoryRes(
        Long id,
        RevenueCustomerType customerType,
        String name,
        String description,
        Boolean isSystem,
        Boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
}
