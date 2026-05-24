package me.nimnakse.water_management.sales.invoices.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import me.nimnakse.water_management.sales.invoices.entity.BillingMethod;
import me.nimnakse.water_management.sales.invoices.entity.SaleType;

public record SalesInvoiceCreateReq(
        Long orgUnitId,
        Long billingZoneId,
        @NotNull(message = "saleType is required") SaleType saleType,
        @NotNull(message = "billingMethod is required") BillingMethod billingMethod,
        String customerName,
        String customerNic,
        String customerAddress,
        String customerMobile,
        @Valid List<SalesInvoiceRevenueLineReq> revenueLines,
        @Valid List<SalesInvoiceInventoryItemReq> inventoryItems,
        @Valid SalesInvoiceInventoryPolicyReq inventoryPolicy,
        @Valid SalesInvoiceInstallmentSetupReq installmentSetup,
        List<Long> selectedConnectionIds,
        List<String> selectedConnectionAccountNumbers,
        Boolean acceptedConnectionSelection
) {
}
