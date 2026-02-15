package me.nimnakse.water_management.sales.invoices.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import me.nimnakse.water_management.revenue.accounts.entity.RevenueAccount;
import me.nimnakse.water_management.sales.invoices.entity.SaleType;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceInventoryPolicy;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceInventoryRecordAs;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceInventoryItem;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceRevenueLine;
import org.junit.jupiter.api.Test;

class SalesInvoiceRulesTest {

    @Test
    void calculatesTotalsWithSalesExpense() {
        SalesInvoiceRevenueLine line = new SalesInvoiceRevenueLine();
        line.setAmount(new BigDecimal("100.00"));

        SalesInvoiceInventoryItem inventoryItem = new SalesInvoiceInventoryItem();
        inventoryItem.setAmount(new BigDecimal("50.00"));

        SalesInvoiceInventoryPolicy policy = new SalesInvoiceInventoryPolicy();
        policy.setRecordAs(SalesInvoiceInventoryRecordAs.SALES_EXPENSE);
        policy.setChargedFromCustomer(Boolean.TRUE);

        SalesInvoiceTotals totals = SalesInvoiceRules.calculateTotals(
                SaleType.CUSTOMER,
                List.of(line),
                List.of(inventoryItem),
                policy);

        assertEquals(new BigDecimal("100.00"), totals.revenueTotal());
        assertEquals(new BigDecimal("50.00"), totals.salesExpenseTotal());
        assertEquals(new BigDecimal("0.00"), totals.consumptionExpenseTotal());
        assertEquals(new BigDecimal("150.00"), totals.grandTotalPayable());
    }

    @Test
    void buildsInstallmentPlanWithLastRemainder() {
        List<SalesInvoiceInstallmentPlanItem> plan = SalesInvoiceRules.buildInstallmentPlan(
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                3);

        assertEquals(4, plan.size());
        assertEquals(new BigDecimal("10.00"), plan.get(0).amount());
        assertEquals(new BigDecimal("30.00"), plan.get(1).amount());
        assertEquals(new BigDecimal("30.00"), plan.get(2).amount());
        assertEquals(new BigDecimal("30.00"), plan.get(3).amount());
    }

    @Test
    void detectsNewConnectionFee() {
        RevenueAccount account = new RevenueAccount();
        account.setFunctionKey("NEW_CONNECTION_FEE");

        SalesInvoiceRevenueLine line = new SalesInvoiceRevenueLine();
        line.setTypeLabel("Any");
        line.setRevenueAccount(account);
        line.setAmount(new BigDecimal("1.00"));

        assertTrue(SalesInvoiceRules.hasNewConnectionFee(List.of(line)));
    }
}
