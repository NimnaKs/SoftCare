package me.nimnakse.water_management.agencies.bill_payment.dto.response;

import java.math.BigDecimal;

public record AgencyBalanceRes(
        Long agencyId,
        String agencyName,
        BigDecimal walletBalance,
        BigDecimal subscriptionBalance,
        BigDecimal subscriptionFee
) {
}
