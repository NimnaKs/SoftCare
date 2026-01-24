package me.nimnakse.water_management.inventory.initial_stocks.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record InventoryInitialStockUpdateReq(
        @NotNull Long orgUnitId,
        @NotNull Long templateId,
        @NotNull @DecimalMin(value = "0.001", message = "Quantity must be greater than zero")
        BigDecimal quantity,
        @NotNull @DecimalMin(value = "0.00", message = "Unit cost must be zero or positive")
        BigDecimal unitCost
) {
}
