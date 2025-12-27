package me.nimnakse.water_management.expenses.main_categories.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import me.nimnakse.water_management.expenses.ExpenseType;

public record ExpenseMainCategoryUpdateReq(
        @NotNull ExpenseType expenseType,
        @NotNull @Min(1) Integer code,
        @NotBlank @Size(max = 255) String name,
        @Size(max = 500) String description,
        Boolean isSystem,
        Boolean isActive
) {
}
