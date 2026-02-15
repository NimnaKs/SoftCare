package me.nimnakse.water_management.sales.invoices.dto.response;

import java.util.List;

public record SalesInvoiceConnectionPreviewRes(
        long count,
        List<SalesInvoiceConnectionPreviewItemRes> items
) {
}
