package me.nimnakse.water_management.liabilities.main_categories.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import me.nimnakse.water_management.liabilities.LiabilityType;
public record LiabilityMainCategoryUpdateReq(
        @NotNull LiabilityType liabilityType,
        @NotBlank @Size(max = 255) String name,
        @Size(max = 500) String description,
        Boolean isSystem,
        Boolean isActive
) {
}
