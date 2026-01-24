package me.nimnakse.water_management.cash_accounts.dto.response;

import java.math.BigDecimal;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.cash_accounts.entity.MonetaryTransaction;

public record MonetaryAccountStatementRes(
        BigDecimal currentBalance,
        PageResponse<MonetaryTransaction> transactions) {
}
