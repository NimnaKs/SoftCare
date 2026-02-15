package me.nimnakse.water_management.sales.invoices.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SalesInvoiceInventoryItemReq(
        String category1,
        String category2,
        String category3,
        String descriptionSpec,
        @NotNull(message = "qty is required") @DecimalMin(value = "0.00", message = "qty must be >= 0") BigDecimal qty,
        String unit,
        @NotNull(message = "unitCost is required") @DecimalMin(value = "0.00", message = "unitCost must be >= 0") BigDecimal unitCost,
        @NotNull(message = "amount is required") @DecimalMin(value = "0.00", message = "amount must be >= 0") BigDecimal amount
) {
}
