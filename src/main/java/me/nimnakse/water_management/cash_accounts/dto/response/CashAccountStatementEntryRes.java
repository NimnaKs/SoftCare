package me.nimnakse.water_management.cash_accounts.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import me.nimnakse.water_management.cash_accounts.CashAccountStatementDirection;

public record CashAccountStatementEntryRes(
        Long transferId,
        CashAccountStatementDirection direction,
        Long counterpartyAccountId,
        String counterpartyAccountName,
        BigDecimal amount,
        String description,
        Instant occurredAt
) {
}
