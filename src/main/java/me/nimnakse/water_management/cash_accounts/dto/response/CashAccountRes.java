package me.nimnakse.water_management.cash_accounts.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import me.nimnakse.water_management.cash_accounts.BankAccountType;
import me.nimnakse.water_management.cash_accounts.MonetaryAccountType;

public record CashAccountRes(
        Long id,
        Long orgUnitId,
        MonetaryAccountType type,
        BankAccountType bankAccountType,
        String accountName,
        String accountNumber,
        String bankName,
        String branchName,
        String branchCode,
        String branchContactNumber,
        BigDecimal openingBalance,
        BigDecimal currentBalance,
        Boolean allowTopup,
        String description,
        Boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
}
