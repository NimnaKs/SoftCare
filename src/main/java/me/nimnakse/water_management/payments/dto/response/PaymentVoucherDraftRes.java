package me.nimnakse.water_management.payments.dto.response;

import java.time.Instant;
import java.util.List;

public record PaymentVoucherDraftRes(
        Long id,
        Long orgUnitId,
        String referenceNo,
        List<PaymentVoucherDraftItemRes> items,
        Instant createdAt,
        Instant updatedAt
) {
}
