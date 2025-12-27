package me.nimnakse.water_management.revenue.accounts.dto.response;

import java.time.Instant;

public record RevenueAccountRes(
        Long id,
        Long mainCategoryId,
        String accountNumber,
        String name,
        String description,
        String referencePrefix,
        Boolean isDefault,
        String functionKey,
        Boolean isSystem,
        Boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
}
