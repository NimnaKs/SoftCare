package me.nimnakse.water_management.liabilities.accounts.dto.response;

import java.time.Instant;

public record LiabilityAccountRes(
        Long id,
        Long mainCategoryId,
        String accountNumber,
        String name,
        String description,
        Boolean isDefault,
        String functionKey,
        Boolean isSystem,
        Boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
}
