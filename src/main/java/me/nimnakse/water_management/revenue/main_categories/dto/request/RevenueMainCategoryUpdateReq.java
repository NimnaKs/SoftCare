package me.nimnakse.water_management.revenue.main_categories.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import me.nimnakse.water_management.revenue.RevenueCustomerType;

public record RevenueMainCategoryUpdateReq(
        @NotNull RevenueCustomerType customerType,
        @NotNull @Min(1) Integer code,
        @NotBlank @Size(max = 255) String name,
        @Size(max = 500) String description,
        Boolean isSystem,
        Boolean isActive
) {
}
