package me.nimnakse.water_management.sales.invoices.dto.response;

import java.util.List;

public record SalesInvoicePostRes(
        List<Long> postedInvoiceIds,
        String message
) {
}
