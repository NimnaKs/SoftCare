package me.nimnakse.water_management.inventory.initial_stocks.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record InventoryInitialStockRes(
        Long id,
        Long orgUnitId,
        Long templateId,
        String templateCode,
        String batchNo,
        BigDecimal quantity,
        Instant createdAt,
        Instant updatedAt
) {
}
