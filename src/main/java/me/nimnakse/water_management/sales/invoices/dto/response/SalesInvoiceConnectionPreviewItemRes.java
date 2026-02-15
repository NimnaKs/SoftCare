package me.nimnakse.water_management.sales.invoices.dto.response;

import me.nimnakse.water_management.connections.entity.ConnectionStatus;

public record SalesInvoiceConnectionPreviewItemRes(
        Long connectionId,
        String premisesNo,
        String accountNumber,
        String name,
        ConnectionStatus status,
        Long billingZoneId
) {
}
