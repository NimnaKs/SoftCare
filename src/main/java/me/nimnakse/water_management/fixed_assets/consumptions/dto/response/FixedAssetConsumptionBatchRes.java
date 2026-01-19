package me.nimnakse.water_management.fixed_assets.consumptions.dto.response;

import java.math.BigDecimal;
import me.nimnakse.water_management.stock.BatchSourceType;

public record FixedAssetConsumptionBatchRes(
        String batchNo,
        BigDecimal remainingQuantity,
        BigDecimal totalAmount,
        BigDecimal unitPrice,
        BatchSourceType sourceType
) {
}
