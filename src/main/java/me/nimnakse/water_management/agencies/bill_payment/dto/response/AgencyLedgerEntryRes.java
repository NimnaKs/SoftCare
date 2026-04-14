package me.nimnakse.water_management.agencies.bill_payment.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record AgencyLedgerEntryRes(
        String ledgerType,
        String referenceText,
        String cashAccountName,
        String paymentMethodName,
        LocalDate paidDate,
        BigDecimal billAmount,
        BigDecimal serviceChargeAmount,
        BigDecimal netAmount,
        BigDecimal walletBalanceAfter,
        BigDecimal subscriptionBalanceAfter,
        Instant createdAt
) {
}
