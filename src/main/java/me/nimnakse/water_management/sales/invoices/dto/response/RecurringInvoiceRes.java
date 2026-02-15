package me.nimnakse.water_management.sales.invoices.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record RecurringInvoiceRes(
        Long id,
        String invoiceNo,
        Long billingZoneId,
        Boolean recurringEnabled,
        BigDecimal grandTotalPayable,
        Instant createdAt,
        Instant updatedAt
) {
}
