package me.nimnakse.water_management.fixed_assets.initial_stocks.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record FixedAssetInitialStockRes(
        Long id,
        Long orgUnitId,
        Long templateId,
        String templateCode,
        String batchNo,
        BigDecimal quantity,
        BigDecimal unitCost,
        Instant createdAt,
        Instant updatedAt
) {
}
