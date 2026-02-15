package me.nimnakse.water_management.sales.invoices.dto.response;

import java.util.List;

public record SalesInvoicePrintableConnectionsRes(
        String invoiceNo,
        long totalItems,
        int totalPages,
        int page,
        int size,
        List<SalesInvoiceConnectionRes> items
) {
}
