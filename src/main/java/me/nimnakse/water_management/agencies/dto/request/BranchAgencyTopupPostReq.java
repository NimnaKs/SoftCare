package me.nimnakse.water_management.agencies.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BranchAgencyTopupPostReq(
        Long cashAccountId,
        Long paymentMethodId,
        String reference,
        LocalDate paidDate,
        BigDecimal amount
) {
}
