package me.nimnakse.water_management.agencies.bill_payment.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AgencyBillPaymentCreateReq(
        String reference,
        LocalDate paidDate,
        BigDecimal amount
) {
}
