package me.nimnakse.water_management.agencies.bill_payment.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record AgencyBillPaymentRes(
        Long id,
        Long agencyId,
        Long cashAccountId,
        String cashAccountName,
        Long paymentMethodId,
        String paymentMethodName,
        String referenceText,
        LocalDate paidDate,
        BigDecimal billAmount,
        BigDecimal serviceChargeAmount,
        BigDecimal totalAmount,
        BigDecimal walletBalanceAfter,
        Instant createdAt
) {
}
