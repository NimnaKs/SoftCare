package me.nimnakse.water_management.purchases.dto.response;

import java.math.BigDecimal;

public record PurchaseOrderItemRes(
        Long id,
        Long inventoryItemId,
        BigDecimal quantity,
        BigDecimal unitCost,
        BigDecimal totalAmount
) {
}
