package me.nimnakse.water_management.cash_accounts.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import me.nimnakse.water_management.cash_accounts.CashAccountStatementDirection;
import me.nimnakse.water_management.cash_accounts.dto.request.CashAccountTransferCreateReq;
import me.nimnakse.water_management.cash_accounts.dto.response.CashAccountStatementEntryRes;
import me.nimnakse.water_management.cash_accounts.dto.response.CashAccountTransferRes;
import me.nimnakse.water_management.cash_accounts.entity.MonetaryAccount;
import me.nimnakse.water_management.cash_accounts.entity.MonetaryAccountTransfer;
import me.nimnakse.water_management.cash_accounts.repository.MonetaryAccountRepository;
import me.nimnakse.water_management.cash_accounts.repository.MonetaryAccountTransferRepository;
import me.nimnakse.water_management.cash_accounts.service.CashAccountTransferService;
import me.nimnakse.water_management.cash_accounts.service.MonetaryTransactionService;
import me.nimnakse.water_management.cash_accounts.entity.MonetaryTransaction;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.security.OrganizationAccessService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CashAccountTransferServiceImpl implements CashAccountTransferService {
    private final MonetaryAccountRepository accountRepository;
    private final MonetaryAccountTransferRepository transferRepository;
    private final MonetaryTransactionService transactionService;
    private final OrganizationAccessService organizationAccessService;

    public CashAccountTransferServiceImpl(MonetaryAccountRepository accountRepository,
            MonetaryAccountTransferRepository transferRepository,
            MonetaryTransactionService transactionService,
            OrganizationAccessService organizationAccessService) {
        this.accountRepository = accountRepository;
        this.transferRepository = transferRepository;
        this.transactionService = transactionService;
        this.organizationAccessService = organizationAccessService;
    }

    @Transactional
    @Override
    public CashAccountTransferRes create(CashAccountTransferCreateReq request) {
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new BadRequestException("Source and destination accounts must be different");
        }
        MonetaryAccount fromAccount = accountRepository.findById(request.fromAccountId())
                .orElseThrow(() -> new NotFoundException("Cash account not found", ErrorCode.NOT_FOUND));
        MonetaryAccount toAccount = accountRepository.findById(request.toAccountId())
                .orElseThrow(() -> new NotFoundException("Cash account not found", ErrorCode.NOT_FOUND));
        organizationAccessService.enforceOrgUnitAccess(fromAccount.getOrgUnitId());
        organizationAccessService.enforceOrgUnitAccess(toAccount.getOrgUnitId());
        if (!Boolean.TRUE.equals(fromAccount.getIsActive()) || !Boolean.TRUE.equals(toAccount.getIsActive())) {
            throw new BadRequestException("Both cash accounts must be active to transfer funds");
        }
        BigDecimal amount = request.amount();
        if (fromAccount.getCurrentBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient balance for the transfer");
        }
        fromAccount.setCurrentBalance(fromAccount.getCurrentBalance().subtract(amount));
        toAccount.setCurrentBalance(toAccount.getCurrentBalance().add(amount));
        MonetaryAccountTransfer transfer = new MonetaryAccountTransfer();
        transfer.setFromAccount(fromAccount);
        transfer.setToAccount(toAccount);
        transfer.setAmount(amount);
        transfer.setDescription(trimToNull(request.description()));
        MonetaryAccountTransfer saved = transferRepository.save(transfer);

        // Record Ledger Transactions
        transactionService.recordTransaction(
                fromAccount.getId(),
                amount.negate(),
                MonetaryTransaction.TransactionType.TRANSFER_OUT,
                null,
                "Transfer to " + toAccount.getAccountName() + ": " + transfer.getDescription(),
                saved.getId());
        transactionService.recordTransaction(
                toAccount.getId(),
                amount,
                MonetaryTransaction.TransactionType.TRANSFER_IN,
                null,
                "Transfer from " + fromAccount.getAccountName() + ": " + transfer.getDescription(),
                saved.getId());

        return toTransferResponse(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<CashAccountStatementEntryRes> getStatement(Long accountId,
            Instant startAt,
            Instant endAt,
            int page,
            int size) {
        if (startAt != null && endAt != null && startAt.isAfter(endAt)) {
            throw new BadRequestException("Start time must be before end time");
        }
        MonetaryAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Cash account not found", ErrorCode.NOT_FOUND));
        organizationAccessService.enforceOrgUnitAccess(account.getOrgUnitId());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<MonetaryAccountTransfer> transfers = transferRepository.findStatementEntries(
                accountId,
                startAt,
                endAt,
                pageRequest);
        var items = transfers.getContent().stream()
                .map(transfer -> toStatementEntry(accountId, transfer))
                .toList();
        return new PageResponse<>(
                items,
                transfers.getTotalElements(),
                transfers.getTotalPages(),
                transfers.getNumber(),
                transfers.getSize());
    }

    private CashAccountTransferRes toTransferResponse(MonetaryAccountTransfer transfer) {
        MonetaryAccount fromAccount = transfer.getFromAccount();
        MonetaryAccount toAccount = transfer.getToAccount();
        return new CashAccountTransferRes(
                transfer.getId(),
                fromAccount.getId(),
                fromAccount.getAccountName(),
                toAccount.getId(),
                toAccount.getAccountName(),
                transfer.getAmount(),
                transfer.getDescription(),
                transfer.getCreatedAt());
    }

    private CashAccountStatementEntryRes toStatementEntry(Long accountId, MonetaryAccountTransfer transfer) {
        boolean isOutgoing = transfer.getFromAccount().getId().equals(accountId);
        MonetaryAccount counterparty = isOutgoing ? transfer.getToAccount() : transfer.getFromAccount();
        return new CashAccountStatementEntryRes(
                transfer.getId(),
                isOutgoing ? CashAccountStatementDirection.OUT : CashAccountStatementDirection.IN,
                counterparty.getId(),
                counterparty.getAccountName(),
                transfer.getAmount(),
                transfer.getDescription(),
                transfer.getCreatedAt());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
