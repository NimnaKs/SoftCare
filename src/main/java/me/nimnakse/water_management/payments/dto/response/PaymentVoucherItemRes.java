package me.nimnakse.water_management.payments.dto.response;

import java.math.BigDecimal;

public record PaymentVoucherItemRes(
        Long id,
        Long expenseAccountId,
        String description,
        BigDecimal totalAmount
) {
}
