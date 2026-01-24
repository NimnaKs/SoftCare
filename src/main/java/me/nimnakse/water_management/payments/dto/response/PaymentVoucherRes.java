package me.nimnakse.water_management.payments.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PaymentVoucherRes(
        Long id,
        Long orgUnitId,
        String voucherNo,
        Long draftId,
        BigDecimal totalAmount,
        Long fundSourceId,
        String fundSourceName,
        List<PaymentVoucherItemRes> items,
        Instant createdAt,
        Instant updatedAt) {
}
