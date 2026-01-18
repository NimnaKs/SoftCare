package me.nimnakse.water_management.purchases.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PurchaseOrderDraftItemCreateReq(
        @NotNull Long inventoryItemId,
        @NotNull @DecimalMin(value = "0.001", message = "Quantity must be greater than zero")
        BigDecimal quantity
) {
}
