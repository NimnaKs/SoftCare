package me.nimnakse.water_management.sales.invoices.dto.response;

import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceInventoryRecordAs;

public record SalesInvoiceInventoryPolicyRes(
        SalesInvoiceInventoryRecordAs recordAs,
        Boolean chargedFromCustomer
) {
}
