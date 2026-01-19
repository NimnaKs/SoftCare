package me.nimnakse.water_management.cash_accounts.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record CashAccountTransferRes(
        Long id,
        Long fromAccountId,
        String fromAccountName,
        Long toAccountId,
        String toAccountName,
        BigDecimal amount,
        String description,
        Instant createdAt
) {
}
