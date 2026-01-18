package me.nimnakse.water_management.purchases.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import me.nimnakse.water_management.purchases.entity.GrnInvoiceStatus;

public record GrnInvoiceRes(
        Long id,
        Long orgUnitId,
        String grnNo,
        Long purchaseOrderId,
        Long supplierId,
        LocalDate grnDate,
        BigDecimal totalAmount,
        GrnInvoiceStatus status,
        List<GrnInvoiceItemRes> items,
        Instant createdAt,
        Instant updatedAt
) {
}
