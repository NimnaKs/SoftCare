package me.nimnakse.water_management.sales.invoices.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SalesInvoiceRevenueLineReq(
        @NotBlank(message = "typeLabel is required") String typeLabel,
        @NotNull(message = "revenueAccountId is required") Long revenueAccountId,
        String description,
        @NotNull(message = "amount is required") @DecimalMin(value = "0.01", message = "amount must be greater than 0") BigDecimal amount
) {
}
