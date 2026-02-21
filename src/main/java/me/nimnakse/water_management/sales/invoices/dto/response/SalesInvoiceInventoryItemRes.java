package me.nimnakse.water_management.sales.invoices.dto.response;

import java.math.BigDecimal;

public record SalesInvoiceInventoryItemRes(
        Long id,
        Long inventoryTemplateId,
        String batchNo,
        String category1,
        String category2,
        String category3,
        String descriptionSpec,
        BigDecimal qty,
        String unit,
        BigDecimal unitCost,
        BigDecimal amount
) {
}
