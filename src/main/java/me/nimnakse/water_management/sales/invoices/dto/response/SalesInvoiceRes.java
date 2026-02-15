package me.nimnakse.water_management.sales.invoices.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import me.nimnakse.water_management.sales.invoices.entity.BillingMethod;
import me.nimnakse.water_management.sales.invoices.entity.SaleType;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceStatus;

public record SalesInvoiceRes(
        Long id,
        String invoiceNo,
        SaleType saleType,
        BillingMethod billingMethod,
        SalesInvoiceStatus status,
        Long orgUnitId,
        Long billingZoneId,
        String customerName,
        String customerNic,
        String customerAddress,
        String customerMobile,
        BigDecimal revenueTotal,
        BigDecimal salesExpenseTotal,
        BigDecimal consumptionExpenseTotal,
        BigDecimal grandTotalPayable,
        Boolean hasInventoryIssue,
        Boolean isRecurring,
        Boolean recurringEnabled,
        BigDecimal downPayment,
        Integer numberOfInstallments,
        List<SalesInvoiceRevenueLineRes> revenueLines,
        List<SalesInvoiceInventoryItemRes> inventoryItems,
        SalesInvoiceInventoryPolicyRes inventoryPolicy,
        List<SalesInvoiceInstallmentRes> installments,
        List<SalesInvoiceConnectionRes> connections,
        Instant createdAt,
        Instant updatedAt
) {
}
