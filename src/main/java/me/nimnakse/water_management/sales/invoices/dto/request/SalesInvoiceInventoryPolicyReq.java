package me.nimnakse.water_management.sales.invoices.dto.request;

import jakarta.validation.constraints.NotNull;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceInventoryRecordAs;

public record SalesInvoiceInventoryPolicyReq(
        @NotNull(message = "recordAs is required") SalesInvoiceInventoryRecordAs recordAs,
        @NotNull(message = "chargedFromCustomer is required") Boolean chargedFromCustomer
) {
}
