package me.nimnakse.water_management.expenses.accounts.dto.response;

import java.time.Instant;

public record ExpenseAccountRes(
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
