package me.nimnakse.water_management.expenses.accounts.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ExpenseAccountUpdateReq(
        @NotNull Long mainCategoryId,
        String accountCode,
        @NotBlank @Size(max = 255) String name,
        @Size(max = 500) String description,
        Boolean isDefault,
        Boolean isSystem,
        Boolean isActive
) {
}
