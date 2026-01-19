package me.nimnakse.water_management.cash_accounts.service.impl;

import java.math.BigDecimal;
import me.nimnakse.water_management.cash_accounts.BankAccountType;
import me.nimnakse.water_management.cash_accounts.MonetaryAccountType;
import me.nimnakse.water_management.cash_accounts.dto.request.CashAccountCreateReq;
import me.nimnakse.water_management.cash_accounts.dto.request.CashAccountUpdateReq;
import me.nimnakse.water_management.cash_accounts.dto.response.CashAccountRes;
import me.nimnakse.water_management.cash_accounts.entity.MonetaryAccount;
import me.nimnakse.water_management.cash_accounts.repository.MonetaryAccountRepository;
import me.nimnakse.water_management.cash_accounts.service.CashAccountService;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.organization.repository.OrgUnitRepository;
import me.nimnakse.water_management.security.OrganizationAccessService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CashAccountServiceImpl implements CashAccountService {
    private final MonetaryAccountRepository accountRepository;
    private final OrgUnitRepository orgUnitRepository;
    private final OrganizationAccessService organizationAccessService;

    public CashAccountServiceImpl(MonetaryAccountRepository accountRepository,
                                  OrgUnitRepository orgUnitRepository,
                                  OrganizationAccessService organizationAccessService) {
        this.accountRepository = accountRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.organizationAccessService = organizationAccessService;
    }

    @Transactional
    @Override
    public CashAccountRes create(CashAccountCreateReq request) {
        validateOrgUnit(request.orgUnitId());
        organizationAccessService.enforceOrgUnitAccess(request.orgUnitId());
        String normalizedName = request.accountName().trim();
        if (accountRepository.existsByOrgUnitIdAndAccountNameIgnoreCase(request.orgUnitId(), normalizedName)) {
            throw new BadRequestException("Account name already exists for the org unit");
        }
        MonetaryAccount account = new MonetaryAccount();
        applyRequest(account, request.type(), request.bankAccountType(), normalizedName, request.accountNumber(),
                request.bankName(), request.branchName(), request.branchCode(), request.branchContactNumber(),
                request.openingBalance(), request.description());
        account.setOrgUnitId(request.orgUnitId());
        account.setCurrentBalance(request.openingBalance());
        account.setIsActive(Boolean.TRUE);
        MonetaryAccount saved = accountRepository.save(account);
        return toResponse(saved);
    }

    @Transactional
    @Override
    public CashAccountRes update(Long id, CashAccountUpdateReq request) {
        MonetaryAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cash account not found", ErrorCode.NOT_FOUND));
        validateOrgUnit(request.orgUnitId());
        organizationAccessService.enforceOrgUnitAccess(request.orgUnitId());
        String normalizedName = request.accountName().trim();
        if (accountRepository.existsByOrgUnitIdAndAccountNameIgnoreCaseAndIdNot(
                request.orgUnitId(), normalizedName, account.getId())) {
            throw new BadRequestException("Account name already exists for the org unit");
        }
        applyRequest(account, request.type(), request.bankAccountType(), normalizedName, request.accountNumber(),
                request.bankName(), request.branchName(), request.branchCode(), request.branchContactNumber(),
                request.openingBalance(), request.description());
        account.setOrgUnitId(request.orgUnitId());
        if (request.isActive() != null) {
            account.setIsActive(request.isActive());
        }
        return toResponse(account);
    }

    @Transactional(readOnly = true)
    @Override
    public CashAccountRes getById(Long id) {
        MonetaryAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cash account not found", ErrorCode.NOT_FOUND));
        organizationAccessService.enforceOrgUnitAccess(account.getOrgUnitId());
        return toResponse(account);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<CashAccountRes> getPage(int page, int size) {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        PageRequest pageRequest = pageRequest(page, size);
        Page<MonetaryAccount> accounts = orgUnitId == null
                ? accountRepository.findAll(pageRequest)
                : accountRepository.findByOrgUnitId(orgUnitId, pageRequest);
        return toPageResponse(accounts);
    }

    @Transactional
    @Override
    public void deactivate(Long id) {
        MonetaryAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cash account not found", ErrorCode.NOT_FOUND));
        organizationAccessService.enforceOrgUnitAccess(account.getOrgUnitId());
        account.setIsActive(Boolean.FALSE);
    }

    private void validateOrgUnit(Long orgUnitId) {
        if (orgUnitId == null || !orgUnitRepository.existsById(orgUnitId)) {
            throw new NotFoundException("Org unit not found", ErrorCode.NOT_FOUND);
        }
    }

    private void applyRequest(MonetaryAccount account,
                              MonetaryAccountType type,
                              BankAccountType bankAccountType,
                              String accountName,
                              String accountNumber,
                              String bankName,
                              String branchName,
                              String branchCode,
                              String branchContactNumber,
                              BigDecimal openingBalance,
                              String description) {
        if (type == MonetaryAccountType.BANK) {
            if (bankAccountType == null) {
                throw new BadRequestException("Bank account type is required for bank accounts");
            }
            if (!StringUtils.hasText(accountNumber) || !StringUtils.hasText(bankName)
                    || !StringUtils.hasText(branchName)) {
                throw new BadRequestException("Account number, bank name, and branch name are required for bank accounts");
            }
        }
        account.setType(type);
        account.setBankAccountType(type == MonetaryAccountType.BANK ? bankAccountType : null);
        account.setAccountName(accountName);
        account.setAccountNumber(type == MonetaryAccountType.BANK ? trimToNull(accountNumber) : null);
        account.setBankName(type == MonetaryAccountType.BANK ? trimToNull(bankName) : null);
        account.setBranchName(type == MonetaryAccountType.BANK ? trimToNull(branchName) : null);
        account.setBranchCode(type == MonetaryAccountType.BANK ? trimToNull(branchCode) : null);
        account.setBranchContactNumber(type == MonetaryAccountType.BANK ? trimToNull(branchContactNumber) : null);
        account.setOpeningBalance(openingBalance);
        account.setDescription(trimToNull(description));
    }

    private PageResponse<CashAccountRes> toPageResponse(Page<MonetaryAccount> page) {
        var items = page.getContent().stream()
                .map(this::toResponse)
                .toList();
        return new PageResponse<>(items, page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize());
    }

    private PageRequest pageRequest(int page, int size) {
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
    }

    private CashAccountRes toResponse(MonetaryAccount account) {
        return new CashAccountRes(
                account.getId(),
                account.getOrgUnitId(),
                account.getType(),
                account.getBankAccountType(),
                account.getAccountName(),
                account.getAccountNumber(),
                account.getBankName(),
                account.getBranchName(),
                account.getBranchCode(),
                account.getBranchContactNumber(),
                account.getOpeningBalance(),
                account.getCurrentBalance(),
                account.getDescription(),
                account.getIsActive(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
