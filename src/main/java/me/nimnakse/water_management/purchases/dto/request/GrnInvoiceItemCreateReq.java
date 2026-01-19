package me.nimnakse.water_management.purchases.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record GrnInvoiceItemCreateReq(
        @NotNull Long inventoryItemId,
        String batchNo,
        @NotNull @DecimalMin(value = "0.001", message = "Quantity must be greater than zero")
        BigDecimal quantity,
        @NotNull @DecimalMin(value = "0.00", message = "Unit cost must be zero or positive")
        BigDecimal unitCost
) {
}
