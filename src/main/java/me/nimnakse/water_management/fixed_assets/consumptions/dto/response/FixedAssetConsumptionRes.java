package me.nimnakse.water_management.fixed_assets.consumptions.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import me.nimnakse.water_management.stock.BatchSourceType;

public record FixedAssetConsumptionRes(
        Long id,
        Long fixedAssetTemplateId,
        Long expenseAccountId,
        BigDecimal quantity,
        BigDecimal totalAmount,
        LocalDate consumedAt,
        String batchNo,
        BatchSourceType batchSource,
        String referenceNo,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
