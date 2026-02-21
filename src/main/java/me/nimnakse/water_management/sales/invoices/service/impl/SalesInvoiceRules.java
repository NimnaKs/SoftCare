package me.nimnakse.water_management.sales.invoices.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import me.nimnakse.water_management.sales.invoices.entity.SaleType;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceInventoryPolicy;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceInventoryRecordAs;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceInstallmentStatus;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceInventoryItem;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceRevenueLine;

final class SalesInvoiceRules {
    private SalesInvoiceRules() {
    }

    static SalesInvoiceTotals calculateTotals(
            SaleType saleType,
            List<SalesInvoiceRevenueLine> revenueLines,
            List<SalesInvoiceInventoryItem> inventoryItems,
            SalesInvoiceInventoryPolicy inventoryPolicy) {
        BigDecimal revenueTotal = revenueLines.stream()
                .map(SalesInvoiceRevenueLine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal inventoryTotal = inventoryItems.stream()
                .map(SalesInvoiceInventoryItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal salesExpenseTotal = BigDecimal.ZERO;
        BigDecimal consumptionExpenseTotal = BigDecimal.ZERO;

        if (inventoryPolicy != null && inventoryTotal.compareTo(BigDecimal.ZERO) > 0) {
            SalesInvoiceInventoryRecordAs recordAs = inventoryPolicy.getRecordAs();
            boolean chargedFromCustomer = Boolean.TRUE.equals(inventoryPolicy.getChargedFromCustomer());

            if (recordAs == SalesInvoiceInventoryRecordAs.SALES_EXPENSE) {
                if (saleType == SaleType.NON_CUSTOMER || chargedFromCustomer) {
                    salesExpenseTotal = inventoryTotal;
                } else {
                    consumptionExpenseTotal = inventoryTotal;
                }
            } else if (recordAs == SalesInvoiceInventoryRecordAs.SALES_REVENUE) {
                salesExpenseTotal = inventoryTotal;
            } else {
                consumptionExpenseTotal = inventoryTotal;
            }
        }

        BigDecimal grandTotalPayable = revenueTotal.add(salesExpenseTotal);
        return new SalesInvoiceTotals(
                scaleMoney(revenueTotal),
                scaleMoney(salesExpenseTotal),
                scaleMoney(consumptionExpenseTotal),
                scaleMoney(grandTotalPayable));
    }

    static List<SalesInvoiceInstallmentPlanItem> buildInstallmentPlan(
            BigDecimal totalPayable,
            BigDecimal downPayment,
            int numberOfInstallments,
            LocalDate downPaymentDate,
            Integer installmentStartYear,
            Integer installmentStartMonth) {
        List<SalesInvoiceInstallmentPlanItem> plan = new ArrayList<>();
        BigDecimal safeTotal = scaleMoney(totalPayable);
        BigDecimal safeDownPayment = scaleMoney(downPayment);
        BigDecimal balance = safeTotal.subtract(safeDownPayment);
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            balance = BigDecimal.ZERO;
        }

        LocalDate safeDownPaymentDate = downPaymentDate == null ? LocalDate.now() : downPaymentDate;
        int cycleDay = safeDownPaymentDate.getDayOfMonth();
        int startYear = installmentStartYear == null ? safeDownPaymentDate.getYear() : installmentStartYear;
        int startMonth = installmentStartMonth == null ? safeDownPaymentDate.getMonthValue() : installmentStartMonth;
        LocalDate firstInstallmentDate = atMonthCycle(startYear, startMonth, cycleDay);

        plan.add(new SalesInvoiceInstallmentPlanItem(
                0,
                "Down Payment",
                safeDownPayment,
                safeDownPaymentDate,
                SalesInvoiceInstallmentStatus.POSTED));

        if (numberOfInstallments <= 0) {
            return plan;
        }

        BigDecimal base = balance.divide(BigDecimal.valueOf(numberOfInstallments), 2, RoundingMode.DOWN);
        BigDecimal running = BigDecimal.ZERO;
        for (int i = 1; i <= numberOfInstallments; i++) {
            BigDecimal amount = i == numberOfInstallments
                    ? balance.subtract(running)
                    : base;
            amount = scaleMoney(amount);
            running = running.add(amount);
            plan.add(new SalesInvoiceInstallmentPlanItem(
                    i,
                    ordinal(i) + " Installment",
                    amount,
                    firstInstallmentDate.plusMonths(i - 1L),
                    SalesInvoiceInstallmentStatus.PLANNED));
        }
        return plan;
    }

    static boolean hasNewConnectionFee(List<SalesInvoiceRevenueLine> lines) {
        return lines.stream().anyMatch(line -> {
            String typeLabel = line.getTypeLabel();
            String functionKey = line.getRevenueAccount().getFunctionKey();
            boolean isTypeLabelNewConnection = typeLabel != null
                    && typeLabel.trim().equalsIgnoreCase("New Connection");
            boolean isFunctionKeyNewConnection = functionKey != null
                    && functionKey.trim().toUpperCase(Locale.ROOT).equals("NEW_CONNECTION_FEE");
            return isTypeLabelNewConnection || isFunctionKeyNewConnection;
        });
    }

    private static String ordinal(int number) {
        if (number % 100 >= 11 && number % 100 <= 13) {
            return number + "th";
        }
        return switch (number % 10) {
            case 1 -> number + "st";
            case 2 -> number + "nd";
            case 3 -> number + "rd";
            default -> number + "th";
        };
    }

    private static LocalDate atMonthCycle(int year, int month, int day) {
        YearMonth ym = YearMonth.of(year, month);
        int validDay = Math.min(day, ym.lengthOfMonth());
        return ym.atDay(validDay);
    }

    private static BigDecimal scaleMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }
}
