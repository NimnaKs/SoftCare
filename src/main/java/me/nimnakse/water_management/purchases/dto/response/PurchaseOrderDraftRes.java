package me.nimnakse.water_management.purchases.dto.response;

import java.time.Instant;
import java.util.List;
public record PurchaseOrderDraftRes(
        Long id,
        Long orgUnitId,
        String referenceNo,
        List<PurchaseOrderDraftItemRes> items,
        Instant createdAt,
        Instant updatedAt
) {
}
