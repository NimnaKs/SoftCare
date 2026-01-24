package me.nimnakse.water_management.purchases.vouchers.dto.response;

import java.time.Instant;
import java.util.List;

public record PurchaseVoucherDraftRes(
        Long id,
        Long orgUnitId,
        String referenceNo,
        List<PurchaseVoucherDraftItemRes> items,
        Instant createdAt,
        Instant updatedAt) {
}
