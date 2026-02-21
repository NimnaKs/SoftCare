package me.nimnakse.water_management.sales.invoices.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record SalesInvoiceInstallmentSetupReq(
        @NotNull(message = "downPayment is required") @DecimalMin(value = "0.00", message = "downPayment must be >= 0") BigDecimal downPayment,
        @NotNull(message = "numberOfInstallments is required") @Min(value = 1, message = "numberOfInstallments must be at least 1") Integer numberOfInstallments,
        @NotNull(message = "downPaymentDate is required") LocalDate downPaymentDate,
        @NotNull(message = "installmentStartYear is required") @Min(value = 2000, message = "installmentStartYear must be >= 2000") @Max(value = 2100, message = "installmentStartYear must be <= 2100") Integer installmentStartYear,
        @NotNull(message = "installmentStartMonth is required") @Min(value = 1, message = "installmentStartMonth must be between 1 and 12") @Max(value = 12, message = "installmentStartMonth must be between 1 and 12") Integer installmentStartMonth
) {
}
