package me.nimnakse.water_management.inventory.consumptions.dto.response;

import java.math.BigDecimal;
import me.nimnakse.water_management.stock.BatchSourceType;

public record InventoryConsumptionBatchRes(
        String batchNo,
        BigDecimal remainingQuantity,
        BigDecimal totalAmount,
        BigDecimal unitPrice,
        BatchSourceType sourceType
) {
}
