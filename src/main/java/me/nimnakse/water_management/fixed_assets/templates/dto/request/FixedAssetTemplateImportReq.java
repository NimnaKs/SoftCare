package me.nimnakse.water_management.fixed_assets.templates.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record FixedAssetTemplateImportReq(
        @NotNull(message = "Org unit id is required") Long orgUnitId,
        @NotEmpty(message = "At least one template id must be provided") List<Long> templateIds
) {
}
