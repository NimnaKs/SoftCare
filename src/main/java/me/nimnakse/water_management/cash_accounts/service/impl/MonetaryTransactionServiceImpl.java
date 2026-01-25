package me.nimnakse.water_management.cash_accounts.service.impl;

import me.nimnakse.water_management.cash_accounts.dto.response.MonetaryAccountStatementRes;
import me.nimnakse.water_management.cash_accounts.entity.MonetaryAccount;
import me.nimnakse.water_management.cash_accounts.entity.MonetaryTransaction;
import me.nimnakse.water_management.cash_accounts.repository.MonetaryAccountRepository;
import me.nimnakse.water_management.cash_accounts.repository.MonetaryTransactionRepository;
import me.nimnakse.water_management.cash_accounts.service.MonetaryTransactionService;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class MonetaryTransactionServiceImpl implements MonetaryTransactionService {

    private final MonetaryTransactionRepository transactionRepository;
    private final MonetaryAccountRepository accountRepository;

    public MonetaryTransactionServiceImpl(MonetaryTransactionRepository transactionRepository,
            MonetaryAccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    @Override
    public void recordTransaction(Long accountId, BigDecimal amount, MonetaryTransaction.TransactionType type,
            String referenceNo, String description, Long sourceId) {
        MonetaryTransaction transaction = new MonetaryTransaction();
        transaction.setAccountId(accountId);
        transaction.setAmount(amount);
        transaction.setTransactionType(type);
        transaction.setReferenceNo(referenceNo);
        transaction.setDescription(description);
        transaction.setTransactionDate(Instant.now());
        transaction.setSourceId(sourceId);
        transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    @Override
    public MonetaryAccountStatementRes getStatement(Long accountId, Instant startAt, Instant endAt, int page,
            int size) {
        MonetaryAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Cash account not found", "මුදල් ගිණුම හමු නොවීය",
                        ErrorCode.NOT_FOUND));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate"));
        Page<MonetaryTransaction> transactions;
        if (startAt != null && endAt != null) {
            transactions = transactionRepository.findByAccountIdAndTransactionDateBetween(accountId, startAt, endAt,
                    pageRequest);
        } else {
            transactions = transactionRepository.findByAccountId(accountId, pageRequest);
        }

        PageResponse<MonetaryTransaction> pageResponse = new PageResponse<>(
                transactions.getContent(),
                transactions.getTotalElements(),
                transactions.getTotalPages(),
                transactions.getNumber(),
                transactions.getSize());

        return new MonetaryAccountStatementRes(account.getCurrentBalance(), pageResponse);
    }
}
