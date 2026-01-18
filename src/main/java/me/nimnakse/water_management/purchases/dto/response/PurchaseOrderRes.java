package me.nimnakse.water_management.purchases.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import me.nimnakse.water_management.purchases.entity.PurchaseOrderStatus;

public record PurchaseOrderRes(
        Long id,
        Long orgUnitId,
        String purchaseOrderNo,
        Long draftId,
        Long supplierId,
        PurchaseOrderStatus status,
        BigDecimal totalAmount,
        List<PurchaseOrderItemRes> items,
        Instant createdAt,
        Instant updatedAt
) {
}
