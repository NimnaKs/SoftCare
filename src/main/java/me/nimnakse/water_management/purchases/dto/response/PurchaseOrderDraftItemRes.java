package me.nimnakse.water_management.purchases.dto.response;

import java.math.BigDecimal;

public record PurchaseOrderDraftItemRes(
        Long id,
        Long inventoryItemId,
        Long fixedAssetTemplateId,
        BigDecimal quantity,
        BigDecimal unitCost,
        BigDecimal totalAmount
) {
}
