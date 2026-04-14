package me.nimnakse.water_management.agencies.bill_payment.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AgencyBillPaymentCreateReq(
        Long connectionId,
        String reference,
        LocalDate paidDate,
        BigDecimal amount,
        List<AgencyBillPaymentSettlementReq> settlements
) {
}
