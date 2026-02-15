package me.nimnakse.water_management.sales.invoices.service.impl;

import java.math.BigDecimal;

record SalesInvoiceTotals(
        BigDecimal revenueTotal,
        BigDecimal salesExpenseTotal,
        BigDecimal consumptionExpenseTotal,
        BigDecimal grandTotalPayable
) {
}
