package me.nimnakse.water_management.purchases.dto.request;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public record PurchaseOrderDraftItemCreateReq(
        Long inventoryItemId,
        Long fixedAssetTemplateId,
        @NotNull @DecimalMin(value = "0.001", message = "Quantity must be greater than zero")
        BigDecimal quantity
) {
}
