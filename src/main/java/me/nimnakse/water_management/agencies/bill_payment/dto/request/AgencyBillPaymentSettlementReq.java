package me.nimnakse.water_management.agencies.bill_payment.dto.request;

import java.math.BigDecimal;

public record AgencyBillPaymentSettlementReq(
        Long invoiceId,
        Long installmentId,
        BigDecimal settledAmount
) {
}
