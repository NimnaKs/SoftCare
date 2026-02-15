package me.nimnakse.water_management.sales.invoices.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SalesInvoiceInstallmentSetupReq(
        @NotNull(message = "downPayment is required") @DecimalMin(value = "0.00", message = "downPayment must be >= 0") BigDecimal downPayment,
        @NotNull(message = "numberOfInstallments is required") @Min(value = 1, message = "numberOfInstallments must be at least 1") Integer numberOfInstallments
) {
}
