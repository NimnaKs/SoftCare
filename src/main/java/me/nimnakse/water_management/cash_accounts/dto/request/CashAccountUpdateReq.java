package me.nimnakse.water_management.cash_accounts.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import me.nimnakse.water_management.cash_accounts.BankAccountType;
import me.nimnakse.water_management.cash_accounts.MonetaryAccountType;

public record CashAccountUpdateReq(
        @NotNull Long orgUnitId,
        @NotNull MonetaryAccountType type,
        BankAccountType bankAccountType,
        @NotBlank @Size(max = 255) String accountName,
        @Size(max = 100) String accountNumber,
        @Size(max = 255) String bankName,
        @Size(max = 255) String branchName,
        @Size(max = 50) String branchCode,
        @Size(max = 30) String branchContactNumber,
        @NotNull BigDecimal openingBalance,
        @Size(max = 500) String description,
        @NotNull List<Long> paymentMethodIds,
        Boolean isActive
) {
}
