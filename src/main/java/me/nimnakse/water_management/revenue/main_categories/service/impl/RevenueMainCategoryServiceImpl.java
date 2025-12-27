package me.nimnakse.water_management.revenue.main_categories.service.impl;

import java.util.List;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.revenue.RevenueCustomerType;
import me.nimnakse.water_management.revenue.main_categories.dto.request.RevenueMainCategoryCreateReq;
import me.nimnakse.water_management.revenue.main_categories.dto.request.RevenueMainCategoryUpdateReq;
import me.nimnakse.water_management.revenue.main_categories.dto.response.RevenueMainCategoryRes;
import me.nimnakse.water_management.revenue.main_categories.entity.RevenueMainCategory;
import me.nimnakse.water_management.revenue.main_categories.repository.RevenueMainCategoryRepository;
import me.nimnakse.water_management.revenue.main_categories.service.RevenueMainCategoryService;
import me.nimnakse.water_management.revenue.accounts.repository.RevenueAccountRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RevenueMainCategoryServiceImpl implements RevenueMainCategoryService {
    private final RevenueMainCategoryRepository mainCategoryRepository;
    private final RevenueAccountRepository accountRepository;

    public RevenueMainCategoryServiceImpl(RevenueMainCategoryRepository mainCategoryRepository,
                                          RevenueAccountRepository accountRepository) {
        this.mainCategoryRepository = mainCategoryRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    @Override
    public RevenueMainCategoryRes create(RevenueMainCategoryCreateReq request) {
        String normalizedName = request.name().trim();
        validateUniqueness(request.code(), null, normalizedName);
        RevenueMainCategory category = new RevenueMainCategory();
        applyRequest(category, request.customerType(), request.code(), normalizedName, request.description(),
                request.isSystem(), request.isActive());
        return toResponse(mainCategoryRepository.save(category));
    }

    @Transactional
    @Override
    public RevenueMainCategoryRes update(Long id, RevenueMainCategoryUpdateReq request) {
        RevenueMainCategory category = mainCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Revenue main category not found", ErrorCode.NOT_FOUND));
        String normalizedName = request.name().trim();
        validateUniqueness(request.code(), id, normalizedName);
        applyRequest(category, request.customerType(), request.code(), normalizedName, request.description(),
                request.isSystem(), request.isActive());
        return toResponse(mainCategoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    @Override
    public RevenueMainCategoryRes getById(Long id) {
        RevenueMainCategory category = mainCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Revenue main category not found", ErrorCode.NOT_FOUND));
        return toResponse(category);
    }

    @Transactional(readOnly = true)
    @Override
    public List<RevenueMainCategoryRes> list() {
        return mainCategoryRepository.findAll(Sort.by(Sort.Direction.ASC, "code", "name")).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long id) {
        RevenueMainCategory category = mainCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Revenue main category not found", ErrorCode.NOT_FOUND));
        if (accountRepository.existsByMainCategoryId(id)) {
            throw new BadRequestException("Revenue accounts exist for this main category");
        }
        mainCategoryRepository.delete(category);
    }

    private void validateUniqueness(Integer code, Long id, String name) {
        if (id == null) {
            if (mainCategoryRepository.existsByCode(code)) {
                throw new BadRequestException("Revenue main category code already exists");
            }
            if (mainCategoryRepository.existsByNameIgnoreCase(name)) {
                throw new BadRequestException("Revenue main category name already exists");
            }
        } else {
            if (mainCategoryRepository.existsByCodeAndIdNot(code, id)) {
                throw new BadRequestException("Revenue main category code already exists");
            }
            if (mainCategoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
                throw new BadRequestException("Revenue main category name already exists");
            }
        }
    }

    private void applyRequest(RevenueMainCategory category,
                              RevenueCustomerType customerType,
                              Integer code,
                              String name,
                              String description,
                              Boolean isSystem,
                              Boolean isActive) {
        category.setCustomerType(customerType);
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

    private RevenueMainCategoryRes toResponse(RevenueMainCategory category) {
        return new RevenueMainCategoryRes(
                category.getId(),
                category.getCustomerType(),
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
