package me.nimnakse.water_management.sales.invoices.dto.response;

import java.math.BigDecimal;

public record SalesInvoiceRevenueLineRes(
        Long id,
        String typeLabel,
        Long revenueAccountId,
        String revenueAccountName,
        String description,
        BigDecimal amount,
        String referenceNo
) {
}
