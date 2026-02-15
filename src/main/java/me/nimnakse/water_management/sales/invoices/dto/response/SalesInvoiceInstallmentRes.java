package me.nimnakse.water_management.sales.invoices.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceInstallmentStatus;

public record SalesInvoiceInstallmentRes(
        Long id,
        Integer installmentNo,
        String label,
        BigDecimal amount,
        LocalDate dueDate,
        SalesInvoiceInstallmentStatus status
) {
}
