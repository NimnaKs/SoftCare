package me.nimnakse.water_management.liabilities.accounts.service.impl;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.liabilities.accounts.dto.request.LiabilityAccountCreateReq;
import me.nimnakse.water_management.liabilities.accounts.dto.request.LiabilityAccountUpdateReq;
import me.nimnakse.water_management.liabilities.accounts.dto.response.LiabilityAccountRes;
import me.nimnakse.water_management.liabilities.accounts.entity.LiabilityAccount;
import me.nimnakse.water_management.liabilities.accounts.repository.LiabilityAccountRepository;
import me.nimnakse.water_management.liabilities.accounts.service.LiabilityAccountService;
import me.nimnakse.water_management.liabilities.main_categories.entity.LiabilityMainCategory;
import me.nimnakse.water_management.liabilities.main_categories.repository.LiabilityMainCategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LiabilityAccountServiceImpl implements LiabilityAccountService {
    private static final Pattern ACCOUNT_NO_PATTERN = Pattern.compile("^LIA-(\\d{4})$");

    private final LiabilityAccountRepository accountRepository;
    private final LiabilityMainCategoryRepository mainCategoryRepository;

    public LiabilityAccountServiceImpl(LiabilityAccountRepository accountRepository,
            LiabilityMainCategoryRepository mainCategoryRepository) {
        this.accountRepository = accountRepository;
        this.mainCategoryRepository = mainCategoryRepository;
    }

    @Transactional
    @Override
    public LiabilityAccountRes create(LiabilityAccountCreateReq request) {
        String normalizedAccountNumber = generateNextAccountNumber();
        String normalizedName = request.name().trim();
        validateUniqueness(normalizedAccountNumber, null, request.mainCategoryId(), normalizedName);
        LiabilityMainCategory mainCategory = getMainCategory(request.mainCategoryId());
        LiabilityAccount account = new LiabilityAccount();
        applyRequest(account, mainCategory, normalizedAccountNumber, normalizedName, request.description(),
                request.isDefault(), request.functionKey(), request.isSystem(), request.isActive());
        return toResponse(accountRepository.save(account));
    }

    @Transactional
    @Override
    public LiabilityAccountRes update(Long id, LiabilityAccountUpdateReq request) {
        LiabilityAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Liability account not found", "Liability account not found",
                        ErrorCode.NOT_FOUND));
        String normalizedAccountNumber = request.accountNumber().trim().toUpperCase();
        validateAccountNumberFormat(normalizedAccountNumber);
        String normalizedName = request.name().trim();
        validateUniqueness(normalizedAccountNumber, id, request.mainCategoryId(), normalizedName);
        LiabilityMainCategory mainCategory = getMainCategory(request.mainCategoryId());
        applyRequest(account, mainCategory, normalizedAccountNumber, normalizedName, request.description(),
                request.isDefault(), request.functionKey(), request.isSystem(), request.isActive());
        return toResponse(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    @Override
    public LiabilityAccountRes getById(Long id) {
        LiabilityAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Liability account not found", "Liability account not found",
                        ErrorCode.NOT_FOUND));
        return toResponse(account);
    }

    @Transactional(readOnly = true)
    @Override
    public List<LiabilityAccountRes> list(Long mainCategoryId) {
        if (mainCategoryId != null) {
            return accountRepository.findByMainCategoryId(mainCategoryId).stream()
                    .sorted((a, b) -> a.getId().compareTo(b.getId()))
                    .map(this::toResponse)
                    .toList();
        }
        return accountRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long id) {
        LiabilityAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Liability account not found", "Liability account not found",
                        ErrorCode.NOT_FOUND));
        if (Boolean.TRUE.equals(account.getIsSystem())) {
            throw new BadRequestException("System expense accounts cannot be deleted",
                    "System expense accounts cannot be deleted");
        }
        accountRepository.delete(account);
    }

    private void validateUniqueness(String accountNumber, Long id, Long mainCategoryId, String name) {
        if (id == null) {
            if (accountRepository.existsByAccountNumberIgnoreCase(accountNumber)) {
                throw new BadRequestException("Liability account number already exists",
                        "Liability account number already exists");
            }
            if (accountRepository.existsByMainCategoryIdAndNameIgnoreCase(mainCategoryId, name)) {
                throw new BadRequestException("Liability account name already exists for the main category",
                        "Liability account name already exists for the main category");
            }
        } else {
            if (accountRepository.existsByAccountNumberIgnoreCaseAndIdNot(accountNumber, id)) {
                throw new BadRequestException("Liability account number already exists",
                        "Liability account number already exists");
            }
            if (accountRepository.existsByMainCategoryIdAndNameIgnoreCaseAndIdNot(mainCategoryId, name, id)) {
                throw new BadRequestException("Liability account name already exists for the main category",
                        "Liability account name already exists for the main category");
            }
        }
    }

    private void validateAccountNumberFormat(String accountNumber) {
        if (!ACCOUNT_NO_PATTERN.matcher(accountNumber).matches()) {
            throw new BadRequestException("Account number must match LIA-0001 format",
                    "Account number must match LIA-0001 format");
        }
    }

    private String generateNextAccountNumber() {
        int max = 0;
        List<LiabilityAccount> rows = accountRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        for (LiabilityAccount row : rows) {
            String number = row.getAccountNumber();
            if (number == null) {
                continue;
            }
            Matcher matcher = ACCOUNT_NO_PATTERN.matcher(number.trim().toUpperCase());
            if (!matcher.matches()) {
                continue;
            }
            int parsed = Integer.parseInt(matcher.group(1));
            if (parsed > max) {
                max = parsed;
            }
        }
        return String.format("LIA-%04d", max + 1);
    }

    private LiabilityMainCategory getMainCategory(Long id) {
        return mainCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Liability main category not found",
                        "Liability main category not found", ErrorCode.NOT_FOUND));
    }

    private void applyRequest(LiabilityAccount account,
            LiabilityMainCategory mainCategory,
            String accountNumber,
            String name,
            String description,
            Boolean isDefault,
            String functionKey,
            Boolean isSystem,
            Boolean isActive) {
        account.setMainCategory(mainCategory);
        account.setAccountNumber(accountNumber.trim().toUpperCase());
        account.setName(name.trim());
        account.setDescription(trimToNull(description));
        account.setFunctionKey(trimToNull(functionKey));
        if (isSystem != null) {
            account.setIsSystem(isSystem);
        }
        if (isActive != null) {
            account.setIsActive(isActive);
        }
        if (isDefault != null) {
            if (Boolean.TRUE.equals(isDefault)) {
                accountRepository.clearDefaultForMainCategory(mainCategory.getId());
            }
            account.setIsDefault(isDefault);
        }
    }

    private LiabilityAccountRes toResponse(LiabilityAccount account) {
        return new LiabilityAccountRes(
                account.getId(),
                account.getMainCategory().getId(),
                account.getAccountNumber(),
                account.getName(),
                account.getDescription(),
                account.getIsDefault(),
                account.getFunctionKey(),
                account.getIsSystem(),
                account.getIsActive(),
                account.getCreatedAt(),
                account.getUpdatedAt());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
