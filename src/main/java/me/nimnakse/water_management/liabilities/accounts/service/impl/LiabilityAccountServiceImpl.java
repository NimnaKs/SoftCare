package me.nimnakse.water_management.liabilities.accounts.service.impl;

import java.util.List;
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
        String normalizedAccountNumber = request.accountNumber().trim();
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
                .orElseThrow(() -> new NotFoundException("Liability account not found", ErrorCode.NOT_FOUND));
        String normalizedAccountNumber = request.accountNumber().trim();
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
                .orElseThrow(() -> new NotFoundException("Liability account not found", ErrorCode.NOT_FOUND));
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
                .orElseThrow(() -> new NotFoundException("Liability account not found", ErrorCode.NOT_FOUND));
        if (Boolean.TRUE.equals(account.getIsSystem())) {
            throw new BadRequestException("System liability accounts cannot be deleted");
        }
        accountRepository.delete(account);
    }

    private void validateUniqueness(String accountNumber, Long id, Long mainCategoryId, String name) {
        if (id == null) {
            if (accountRepository.existsByAccountNumberIgnoreCase(accountNumber)) {
                throw new BadRequestException("Liability account number already exists");
            }
            if (accountRepository.existsByMainCategoryIdAndNameIgnoreCase(mainCategoryId, name)) {
                throw new BadRequestException("Liability account name already exists for the main category");
            }
        } else {
            if (accountRepository.existsByAccountNumberIgnoreCaseAndIdNot(accountNumber, id)) {
                throw new BadRequestException("Liability account number already exists");
            }
            if (accountRepository.existsByMainCategoryIdAndNameIgnoreCaseAndIdNot(mainCategoryId, name, id)) {
                throw new BadRequestException("Liability account name already exists for the main category");
            }
        }
    }

    private LiabilityMainCategory getMainCategory(Long id) {
        return mainCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Liability main category not found", ErrorCode.NOT_FOUND));
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
        account.setAccountNumber(accountNumber.trim());
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
