package me.nimnakse.water_management.inventory.templates.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record InventoryTemplateImportReq(
        @NotNull(message = "Org unit id is required") Long orgUnitId,
        @NotEmpty(message = "At least one template id is required") List<Long> templateIds
) {
}
