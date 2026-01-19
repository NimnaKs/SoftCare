package me.nimnakse.water_management.payments.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PaymentVoucherDraftItemUpdateReq(
        @NotNull Long expenseAccountId,
        String description,
        @NotNull @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal totalAmount
) {
}
