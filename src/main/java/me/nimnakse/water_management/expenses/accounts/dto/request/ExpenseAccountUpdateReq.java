package me.nimnakse.water_management.expenses.accounts.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ExpenseAccountUpdateReq(
        @NotNull Long mainCategoryId,
        @NotBlank @Size(max = 50) String accountNumber,
        @NotBlank @Size(max = 255) String name,
        @Size(max = 500) String description,
        Boolean isDefault,
        @Size(max = 100) String functionKey,
        Boolean isSystem,
        Boolean isActive
) {
}
