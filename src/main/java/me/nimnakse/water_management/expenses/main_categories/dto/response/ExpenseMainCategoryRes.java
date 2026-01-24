package me.nimnakse.water_management.expenses.main_categories.dto.response;

import java.time.Instant;
import me.nimnakse.water_management.expenses.ExpenseType;

public record ExpenseMainCategoryRes(
        Long id,
        ExpenseType expenseType,
        String name,
        String description,
        Boolean isSystem,
        Boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
}
