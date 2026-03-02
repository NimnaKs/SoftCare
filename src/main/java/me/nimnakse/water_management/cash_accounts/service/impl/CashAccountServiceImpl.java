package me.nimnakse.water_management.cash_accounts.service.impl;

import me.nimnakse.water_management.cash_accounts.BankAccountType;
import me.nimnakse.water_management.cash_accounts.MonetaryAccountType;
import me.nimnakse.water_management.cash_accounts.dto.request.CashAccountCreateReq;
import me.nimnakse.water_management.cash_accounts.dto.request.CashAccountUpdateReq;
import me.nimnakse.water_management.cash_accounts.dto.response.CashAccountRes;
import me.nimnakse.water_management.cash_accounts.entity.MonetaryAccount;
import me.nimnakse.water_management.cash_accounts.entity.MonetaryAccountPaymentMethod;
import me.nimnakse.water_management.cash_accounts.entity.MonetaryAccountPaymentMethodId;
import me.nimnakse.water_management.cash_accounts.repository.MonetaryAccountPaymentMethodRepository;
import me.nimnakse.water_management.cash_accounts.repository.MonetaryAccountRepository;
import me.nimnakse.water_management.cash_accounts.service.CashAccountService;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.organization.repository.OrgUnitRepository;
import me.nimnakse.water_management.receipts.PaymentMethodCode;
import me.nimnakse.water_management.receipts.entity.PaymentMethodLookup;
import me.nimnakse.water_management.receipts.repository.PaymentMethodLookupRepository;
import me.nimnakse.water_management.security.OrganizationAccessService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CashAccountServiceImpl implements CashAccountService {
    private final MonetaryAccountRepository accountRepository;
    private final MonetaryAccountPaymentMethodRepository mappingRepository;
    private final PaymentMethodLookupRepository paymentMethodLookupRepository;
    private final OrgUnitRepository orgUnitRepository;
    private final OrganizationAccessService organizationAccessService;

    public CashAccountServiceImpl(
            MonetaryAccountRepository accountRepository,
            MonetaryAccountPaymentMethodRepository mappingRepository,
            PaymentMethodLookupRepository paymentMethodLookupRepository,
            OrgUnitRepository orgUnitRepository,
            OrganizationAccessService organizationAccessService
    ) {
        this.accountRepository = accountRepository;
        this.mappingRepository = mappingRepository;
        this.paymentMethodLookupRepository = paymentMethodLookupRepository;
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
            throw new BadRequestException("Account name already exists for the org unit",
                    "Account name already exists for the org unit", ErrorCode.VALIDATION_ERROR);
        }

        MonetaryAccount account = new MonetaryAccount();
        applyRequest(account, request.type(), request.bankAccountType(), normalizedName, request.accountNumber(),
                request.bankName(), request.branchName(), request.branchCode(), request.branchContactNumber(),
                request.openingBalance(), request.allowTopup(), request.description());
        account.setOrgUnitId(request.orgUnitId());
        account.setCurrentBalance(request.openingBalance());
        account.setIsActive(Boolean.TRUE);

        MonetaryAccount saved = accountRepository.save(account);
        ResolvedPaymentMethods resolved = sanitizeAndResolvePaymentMethods(request.paymentMethods());
        replaceMappings(saved.getId(), resolved.methodIds());
        return toResponse(saved, resolved.codes());
    }

    @Transactional
    @Override
    public CashAccountRes update(Long id, CashAccountUpdateReq request) {
        MonetaryAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cash account not found", "Cash account not found", ErrorCode.NOT_FOUND));

        validateOrgUnit(request.orgUnitId());
        organizationAccessService.enforceOrgUnitAccess(request.orgUnitId());

        String normalizedName = request.accountName().trim();
        if (accountRepository.existsByOrgUnitIdAndAccountNameIgnoreCaseAndIdNot(
                request.orgUnitId(), normalizedName, account.getId())) {
            throw new BadRequestException("Account name already exists for the org unit",
                    "Account name already exists for the org unit", ErrorCode.VALIDATION_ERROR);
        }

        applyRequest(account, request.type(), request.bankAccountType(), normalizedName, request.accountNumber(),
                request.bankName(), request.branchName(), request.branchCode(), request.branchContactNumber(),
                request.openingBalance(), request.allowTopup(), request.description());
        account.setOrgUnitId(request.orgUnitId());
        if (request.isActive() != null) account.setIsActive(request.isActive());

        ResolvedPaymentMethods resolved = sanitizeAndResolvePaymentMethods(request.paymentMethods());
        replaceMappings(account.getId(), resolved.methodIds());
        return toResponse(account, resolved.codes());
    }

    @Transactional(readOnly = true)
    @Override
    public CashAccountRes getById(Long id) {
        MonetaryAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cash account not found", "Cash account not found", ErrorCode.NOT_FOUND));
        organizationAccessService.enforceOrgUnitAccess(account.getOrgUnitId());

        List<PaymentMethodCode> codes = loadCodesByAccount(account.getId());
        return toResponse(account, codes);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<CashAccountRes> getPage(int page, int size) {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<MonetaryAccount> accounts = orgUnitId == null
                ? accountRepository.findAll(pageRequest)
                : accountRepository.findByOrgUnitId(orgUnitId, pageRequest);
        return toPageResponse(accounts);
    }

    @Transactional
    @Override
    public void deactivate(Long id) {
        MonetaryAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cash account not found", "Cash account not found", ErrorCode.NOT_FOUND));
        organizationAccessService.enforceOrgUnitAccess(account.getOrgUnitId());
        account.setIsActive(Boolean.FALSE);
    }

    private void validateOrgUnit(Long orgUnitId) {
        if (orgUnitId == null || !orgUnitRepository.existsById(orgUnitId)) {
            throw new NotFoundException("Org unit not found", "Org unit not found", ErrorCode.NOT_FOUND);
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
                              Boolean allowTopup,
                              String description) {
        if (type == MonetaryAccountType.BANK) {
            if (bankAccountType == null) {
                throw new BadRequestException("Bank account type is required for bank accounts",
                        "Bank account type is required for bank accounts", ErrorCode.VALIDATION_ERROR);
            }
            if (!StringUtils.hasText(accountNumber) || !StringUtils.hasText(bankName) || !StringUtils.hasText(branchName)) {
                throw new BadRequestException("Account number, bank name, and branch name are required for bank accounts",
                        "Account number, bank name, and branch name are required for bank accounts", ErrorCode.VALIDATION_ERROR);
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
        account.setAllowTopup(Boolean.TRUE.equals(allowTopup));
        account.setDescription(trimToNull(description));
    }

    private PageResponse<CashAccountRes> toPageResponse(Page<MonetaryAccount> page) {
        Set<Long> accountIds = page.getContent().stream().map(MonetaryAccount::getId).collect(Collectors.toSet());
        Map<Long, List<Long>> methodIdsByAccount = mappingRepository.findByIdMonetaryAccountIdIn(accountIds).stream()
                .collect(Collectors.groupingBy(
                        mapping -> mapping.getId().getMonetaryAccountId(),
                        Collectors.mapping(mapping -> mapping.getId().getPaymentMethodId(), Collectors.toList())
                ));

        Set<Long> allMethodIds = methodIdsByAccount.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        Map<Long, PaymentMethodCode> codeById = paymentMethodLookupRepository.findAllById(allMethodIds).stream()
                .collect(Collectors.toMap(PaymentMethodLookup::getId, method -> toCode(method.getCode())));

        List<CashAccountRes> items = page.getContent().stream()
                .map(account -> {
                    List<PaymentMethodCode> codes = methodIdsByAccount.getOrDefault(account.getId(), List.of()).stream()
                            .map(codeById::get)
                            .filter(Objects::nonNull)
                            .distinct()
                            .toList();
                    return toResponse(account, codes);
                })
                .toList();
        return new PageResponse<>(items, page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize());
    }

    private CashAccountRes toResponse(MonetaryAccount account, List<PaymentMethodCode> paymentMethods) {
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
                account.getAllowTopup(),
                paymentMethods,
                account.getDescription(),
                account.getIsActive(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    private ResolvedPaymentMethods sanitizeAndResolvePaymentMethods(List<PaymentMethodCode> paymentMethods) {
        if (paymentMethods == null || paymentMethods.isEmpty()) {
            throw new BadRequestException("At least one transaction method is required",
                    "At least one transaction method is required", ErrorCode.VALIDATION_ERROR);
        }

        List<PaymentMethodCode> codes = paymentMethods.stream().filter(Objects::nonNull).distinct().toList();
        if (codes.isEmpty()) {
            throw new BadRequestException("At least one transaction method is required",
                    "At least one transaction method is required", ErrorCode.VALIDATION_ERROR);
        }

        List<Long> methodIds = new ArrayList<>();
        for (PaymentMethodCode code : codes) {
            PaymentMethodLookup lookup = paymentMethodLookupRepository.findByCodeIgnoreCase(code.name())
                    .filter(method -> Boolean.TRUE.equals(method.getIsActive()))
                    .orElseThrow(() -> new BadRequestException("Invalid transaction method selected",
                            "Invalid transaction method selected", ErrorCode.VALIDATION_ERROR));
            methodIds.add(lookup.getId());
        }
        return new ResolvedPaymentMethods(methodIds, codes);
    }

    private void replaceMappings(Long accountId, List<Long> methodIds) {
        mappingRepository.deleteByIdMonetaryAccountId(accountId);
        List<MonetaryAccountPaymentMethod> mappings = methodIds.stream()
                .map(methodId -> {
                    MonetaryAccountPaymentMethod mapping = new MonetaryAccountPaymentMethod();
                    mapping.setId(new MonetaryAccountPaymentMethodId(accountId, methodId));
                    return mapping;
                })
                .toList();
        mappingRepository.saveAll(mappings);
    }

    private List<PaymentMethodCode> loadCodesByAccount(Long accountId) {
        List<Long> methodIds = mappingRepository.findByIdMonetaryAccountId(accountId).stream()
                .map(mapping -> mapping.getId().getPaymentMethodId())
                .toList();
        Map<Long, PaymentMethodCode> codeById = paymentMethodLookupRepository.findAllById(methodIds).stream()
                .collect(Collectors.toMap(PaymentMethodLookup::getId, method -> toCode(method.getCode())));
        return methodIds.stream().map(codeById::get).filter(Objects::nonNull).distinct().toList();
    }

    private PaymentMethodCode toCode(String dbCode) {
        if (!StringUtils.hasText(dbCode)) return null;
        try {
            return PaymentMethodCode.valueOf(dbCode.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record ResolvedPaymentMethods(List<Long> methodIds, List<PaymentMethodCode> codes) {}
}

