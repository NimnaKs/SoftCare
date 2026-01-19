package me.nimnakse.water_management.purchases.dto.response;

import java.math.BigDecimal;

public record GrnInvoiceItemRes(
        Long id,
        Long inventoryItemId,
        Long fixedAssetTemplateId,
        String batchNo,
        BigDecimal quantity,
        BigDecimal unitCost,
        BigDecimal totalAmount
) {
}
