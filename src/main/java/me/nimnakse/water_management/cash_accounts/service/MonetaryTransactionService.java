package me.nimnakse.water_management.cash_accounts.service;

import me.nimnakse.water_management.cash_accounts.entity.MonetaryTransaction;
import me.nimnakse.water_management.cash_accounts.dto.response.MonetaryAccountStatementRes;
import java.time.Instant;
import java.math.BigDecimal;

public interface MonetaryTransactionService {
    void recordTransaction(Long accountId, BigDecimal amount, MonetaryTransaction.TransactionType type,
            String referenceNo, String description, Long sourceId);

    MonetaryAccountStatementRes getStatement(Long accountId, Instant startAt, Instant endAt, int page, int size);
}
