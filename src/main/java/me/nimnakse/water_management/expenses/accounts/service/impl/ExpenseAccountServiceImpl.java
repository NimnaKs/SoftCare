package me.nimnakse.water_management.expenses.accounts.service.impl;

import java.util.List;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.expenses.accounts.dto.request.ExpenseAccountCreateReq;
import me.nimnakse.water_management.expenses.accounts.dto.request.ExpenseAccountUpdateReq;
import me.nimnakse.water_management.expenses.accounts.dto.response.ExpenseAccountRes;
import me.nimnakse.water_management.expenses.accounts.entity.ExpenseAccount;
import me.nimnakse.water_management.expenses.accounts.repository.ExpenseAccountRepository;
import me.nimnakse.water_management.expenses.accounts.service.ExpenseAccountService;
import me.nimnakse.water_management.expenses.main_categories.entity.ExpenseMainCategory;
import me.nimnakse.water_management.expenses.main_categories.repository.ExpenseMainCategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseAccountServiceImpl implements ExpenseAccountService {
    private final ExpenseAccountRepository accountRepository;
    private final ExpenseMainCategoryRepository mainCategoryRepository;

    public ExpenseAccountServiceImpl(ExpenseAccountRepository accountRepository,
                                     ExpenseMainCategoryRepository mainCategoryRepository) {
        this.accountRepository = accountRepository;
        this.mainCategoryRepository = mainCategoryRepository;
    }

    @Transactional
    @Override
    public ExpenseAccountRes create(ExpenseAccountCreateReq request) {
        String normalizedAccountNumber = request.accountNumber().trim();
        String normalizedName = request.name().trim();
        validateUniqueness(normalizedAccountNumber, null, request.mainCategoryId(), normalizedName);
        ExpenseMainCategory mainCategory = getMainCategory(request.mainCategoryId());
        ExpenseAccount account = new ExpenseAccount();
        applyRequest(account, mainCategory, normalizedAccountNumber, normalizedName, request.description(),
                request.isDefault(), request.functionKey(), request.isSystem(), request.isActive());
        return toResponse(accountRepository.save(account));
    }

    @Transactional
    @Override
    public ExpenseAccountRes update(Long id, ExpenseAccountUpdateReq request) {
        ExpenseAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense account not found", ErrorCode.NOT_FOUND));
        String normalizedAccountNumber = request.accountNumber().trim();
        String normalizedName = request.name().trim();
        validateUniqueness(normalizedAccountNumber, id, request.mainCategoryId(), normalizedName);
        ExpenseMainCategory mainCategory = getMainCategory(request.mainCategoryId());
        applyRequest(account, mainCategory, normalizedAccountNumber, normalizedName, request.description(),
                request.isDefault(), request.functionKey(), request.isSystem(), request.isActive());
        return toResponse(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    @Override
    public ExpenseAccountRes getById(Long id) {
        ExpenseAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense account not found", ErrorCode.NOT_FOUND));
        return toResponse(account);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ExpenseAccountRes> list(Long mainCategoryId) {
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
        ExpenseAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense account not found", ErrorCode.NOT_FOUND));
        if (Boolean.TRUE.equals(account.getIsSystem())) {
            throw new BadRequestException("System expense accounts cannot be deleted");
        }
        accountRepository.delete(account);
    }

    private void validateUniqueness(String accountNumber, Long id, Long mainCategoryId, String name) {
        if (id == null) {
            if (accountRepository.existsByAccountNumberIgnoreCase(accountNumber)) {
                throw new BadRequestException("Expense account number already exists");
            }
            if (accountRepository.existsByMainCategoryIdAndNameIgnoreCase(mainCategoryId, name)) {
                throw new BadRequestException("Expense account name already exists for the main category");
            }
        } else {
            if (accountRepository.existsByAccountNumberIgnoreCaseAndIdNot(accountNumber, id)) {
                throw new BadRequestException("Expense account number already exists");
            }
            if (accountRepository.existsByMainCategoryIdAndNameIgnoreCaseAndIdNot(mainCategoryId, name, id)) {
                throw new BadRequestException("Expense account name already exists for the main category");
            }
        }
    }

    private ExpenseMainCategory getMainCategory(Long id) {
        return mainCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense main category not found", ErrorCode.NOT_FOUND));
    }

    private void applyRequest(ExpenseAccount account,
                              ExpenseMainCategory mainCategory,
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

    private ExpenseAccountRes toResponse(ExpenseAccount account) {
        return new ExpenseAccountRes(
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
