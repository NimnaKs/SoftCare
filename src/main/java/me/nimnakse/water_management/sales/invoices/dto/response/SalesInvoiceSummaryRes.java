package me.nimnakse.water_management.sales.invoices.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import me.nimnakse.water_management.sales.invoices.entity.BillingMethod;
import me.nimnakse.water_management.sales.invoices.entity.SaleType;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceStatus;

public record SalesInvoiceSummaryRes(
        Long id,
        String invoiceNo,
        SaleType saleType,
        BillingMethod billingMethod,
        SalesInvoiceStatus status,
        Long billingZoneId,
        BigDecimal grandTotalPayable,
        Boolean isRecurring,
        String createdByUsername,
        Instant createdAt,
        Instant updatedAt
) {
}
