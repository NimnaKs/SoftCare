package me.nimnakse.water_management.purchases.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PurchaseOrderDraftItemUpdateReq(
        @NotNull Long inventoryItemId,
        @DecimalMin(value = "0.00", message = "Unit cost must be zero or positive")
        BigDecimal unitCost
) {
}
