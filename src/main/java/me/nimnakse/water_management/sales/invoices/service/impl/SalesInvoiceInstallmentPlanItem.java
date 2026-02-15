package me.nimnakse.water_management.sales.invoices.service.impl;

import java.math.BigDecimal;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceInstallmentStatus;

record SalesInvoiceInstallmentPlanItem(
        int installmentNo,
        String label,
        BigDecimal amount,
        SalesInvoiceInstallmentStatus status
) {
}
