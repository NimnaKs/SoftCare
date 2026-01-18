package me.nimnakse.water_management.purchases.dto.response;

import java.time.Instant;
import java.util.List;
import me.nimnakse.water_management.purchases.entity.PurchaseOrderDraftStatus;

public record PurchaseOrderDraftRes(
        Long id,
        Long orgUnitId,
        String referenceNo,
        PurchaseOrderDraftStatus status,
        List<PurchaseOrderDraftItemRes> items,
        Instant createdAt,
        Instant updatedAt
) {
}
