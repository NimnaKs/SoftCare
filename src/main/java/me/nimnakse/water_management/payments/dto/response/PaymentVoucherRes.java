package me.nimnakse.water_management.payments.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import me.nimnakse.water_management.payments.entity.PaymentVoucherStatus;

public record PaymentVoucherRes(
        Long id,
        Long orgUnitId,
        String voucherNo,
        Long draftId,
        PaymentVoucherStatus status,
        BigDecimal totalAmount,
        List<PaymentVoucherItemRes> items,
        Instant createdAt,
        Instant updatedAt
) {
}
