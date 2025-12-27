package me.nimnakse.water_management.expenses.main_categories.service.impl;

import java.util.List;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.expenses.ExpenseType;
import me.nimnakse.water_management.expenses.accounts.repository.ExpenseAccountRepository;
import me.nimnakse.water_management.expenses.main_categories.dto.request.ExpenseMainCategoryCreateReq;
import me.nimnakse.water_management.expenses.main_categories.dto.request.ExpenseMainCategoryUpdateReq;
import me.nimnakse.water_management.expenses.main_categories.dto.response.ExpenseMainCategoryRes;
import me.nimnakse.water_management.expenses.main_categories.entity.ExpenseMainCategory;
import me.nimnakse.water_management.expenses.main_categories.repository.ExpenseMainCategoryRepository;
import me.nimnakse.water_management.expenses.main_categories.service.ExpenseMainCategoryService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseMainCategoryServiceImpl implements ExpenseMainCategoryService {
    private final ExpenseMainCategoryRepository mainCategoryRepository;
    private final ExpenseAccountRepository accountRepository;

    public ExpenseMainCategoryServiceImpl(ExpenseMainCategoryRepository mainCategoryRepository,
                                          ExpenseAccountRepository accountRepository) {
        this.mainCategoryRepository = mainCategoryRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    @Override
    public ExpenseMainCategoryRes create(ExpenseMainCategoryCreateReq request) {
        String normalizedName = request.name().trim();
        validateUniqueness(request.code(), null, normalizedName);
        ExpenseMainCategory category = new ExpenseMainCategory();
        applyRequest(category, request.expenseType(), request.code(), normalizedName, request.description(),
                request.isSystem(), request.isActive());
        return toResponse(mainCategoryRepository.save(category));
    }

    @Transactional
    @Override
    public ExpenseMainCategoryRes update(Long id, ExpenseMainCategoryUpdateReq request) {
        ExpenseMainCategory category = mainCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense main category not found", ErrorCode.NOT_FOUND));
        String normalizedName = request.name().trim();
        validateUniqueness(request.code(), id, normalizedName);
        applyRequest(category, request.expenseType(), request.code(), normalizedName, request.description(),
                request.isSystem(), request.isActive());
        return toResponse(mainCategoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    @Override
    public ExpenseMainCategoryRes getById(Long id) {
        ExpenseMainCategory category = mainCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense main category not found", ErrorCode.NOT_FOUND));
        return toResponse(category);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ExpenseMainCategoryRes> list() {
        return mainCategoryRepository.findAll(Sort.by(Sort.Direction.ASC, "code", "name")).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long id) {
        ExpenseMainCategory category = mainCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense main category not found", ErrorCode.NOT_FOUND));
        if (accountRepository.existsByMainCategoryId(id)) {
            throw new BadRequestException("Expense accounts exist for this main category");
        }
        mainCategoryRepository.delete(category);
    }

    private void validateUniqueness(Integer code, Long id, String name) {
        if (id == null) {
            if (mainCategoryRepository.existsByCode(code)) {
                throw new BadRequestException("Expense main category code already exists");
            }
            if (mainCategoryRepository.existsByNameIgnoreCase(name)) {
                throw new BadRequestException("Expense main category name already exists");
            }
        } else {
            if (mainCategoryRepository.existsByCodeAndIdNot(code, id)) {
                throw new BadRequestException("Expense main category code already exists");
            }
            if (mainCategoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
                throw new BadRequestException("Expense main category name already exists");
            }
        }
    }

    private void applyRequest(ExpenseMainCategory category,
                              ExpenseType expenseType,
                              Integer code,
                              String name,
                              String description,
                              Boolean isSystem,
                              Boolean isActive) {
        category.setExpenseType(expenseType);
        category.setCode(code);
        category.setName(name.trim());
        category.setDescription(trimToNull(description));
        if (isSystem != null) {
            category.setIsSystem(isSystem);
        }
        if (isActive != null) {
            category.setIsActive(isActive);
        }
    }

    private ExpenseMainCategoryRes toResponse(ExpenseMainCategory category) {
        return new ExpenseMainCategoryRes(
                category.getId(),
                category.getExpenseType(),
                category.getCode(),
                category.getName(),
                category.getDescription(),
                category.getIsSystem(),
                category.getIsActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
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
