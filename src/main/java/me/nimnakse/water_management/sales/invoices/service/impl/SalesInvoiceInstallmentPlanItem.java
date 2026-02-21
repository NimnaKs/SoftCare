package me.nimnakse.water_management.sales.invoices.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceInstallmentStatus;

record SalesInvoiceInstallmentPlanItem(
        int installmentNo,
        String label,
        BigDecimal amount,
        LocalDate dueDate,
        SalesInvoiceInstallmentStatus status
) {
}
