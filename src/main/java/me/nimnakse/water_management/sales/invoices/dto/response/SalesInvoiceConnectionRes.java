package me.nimnakse.water_management.sales.invoices.dto.response;

public record SalesInvoiceConnectionRes(
        Long id,
        Long connectionId,
        Long billingZoneId,
        String premisesNo,
        String connectionNo,
        String accountNumber,
        String customerName
) {
}
