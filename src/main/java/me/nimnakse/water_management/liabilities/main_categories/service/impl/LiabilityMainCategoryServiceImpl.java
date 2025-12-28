package me.nimnakse.water_management.liabilities.main_categories.service.impl;

import java.util.List;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.liabilities.LiabilityType;
import me.nimnakse.water_management.liabilities.accounts.repository.LiabilityAccountRepository;
import me.nimnakse.water_management.liabilities.main_categories.dto.request.LiabilityMainCategoryCreateReq;
import me.nimnakse.water_management.liabilities.main_categories.dto.request.LiabilityMainCategoryUpdateReq;
import me.nimnakse.water_management.liabilities.main_categories.dto.response.LiabilityMainCategoryRes;
import me.nimnakse.water_management.liabilities.main_categories.entity.LiabilityMainCategory;
import me.nimnakse.water_management.liabilities.main_categories.repository.LiabilityMainCategoryRepository;
import me.nimnakse.water_management.liabilities.main_categories.service.LiabilityMainCategoryService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LiabilityMainCategoryServiceImpl implements LiabilityMainCategoryService {
    private final LiabilityMainCategoryRepository mainCategoryRepository;
    private final LiabilityAccountRepository accountRepository;

    public LiabilityMainCategoryServiceImpl(LiabilityMainCategoryRepository mainCategoryRepository,
                                            LiabilityAccountRepository accountRepository) {
        this.mainCategoryRepository = mainCategoryRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    @Override
    public LiabilityMainCategoryRes create(LiabilityMainCategoryCreateReq request) {
        String normalizedName = request.name().trim();
        validateUniqueness(request.code(), null, normalizedName);
        LiabilityMainCategory category = new LiabilityMainCategory();
        applyRequest(category, request.liabilityType(), request.code(), normalizedName, request.description(),
                request.isSystem(), request.isActive());
        return toResponse(mainCategoryRepository.save(category));
    }

    @Transactional
    @Override
    public LiabilityMainCategoryRes update(Long id, LiabilityMainCategoryUpdateReq request) {
        LiabilityMainCategory category = mainCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Liability main category not found", ErrorCode.NOT_FOUND));
        String normalizedName = request.name().trim();
        validateUniqueness(request.code(), id, normalizedName);
        applyRequest(category, request.liabilityType(), request.code(), normalizedName, request.description(),
                request.isSystem(), request.isActive());
        return toResponse(mainCategoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    @Override
    public LiabilityMainCategoryRes getById(Long id) {
        LiabilityMainCategory category = mainCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Liability main category not found", ErrorCode.NOT_FOUND));
        return toResponse(category);
    }

    @Transactional(readOnly = true)
    @Override
    public List<LiabilityMainCategoryRes> list() {
        return mainCategoryRepository.findAll(Sort.by(Sort.Direction.ASC, "code", "name")).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long id) {
        LiabilityMainCategory category = mainCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Liability main category not found", ErrorCode.NOT_FOUND));
        if (Boolean.TRUE.equals(category.getIsSystem())) {
            throw new BadRequestException("System liability main categories cannot be deleted");
        }
        if (accountRepository.existsByMainCategoryId(id)) {
            throw new BadRequestException("Liability accounts exist for this main category");
        }
        mainCategoryRepository.delete(category);
    }

    private void validateUniqueness(Integer code, Long id, String name) {
        if (id == null) {
            if (mainCategoryRepository.existsByCode(code)) {
                throw new BadRequestException("Liability main category code already exists");
            }
            if (mainCategoryRepository.existsByNameIgnoreCase(name)) {
                throw new BadRequestException("Liability main category name already exists");
            }
        } else {
            if (mainCategoryRepository.existsByCodeAndIdNot(code, id)) {
                throw new BadRequestException("Liability main category code already exists");
            }
            if (mainCategoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
                throw new BadRequestException("Liability main category name already exists");
            }
        }
    }

    private void applyRequest(LiabilityMainCategory category,
                              LiabilityType liabilityType,
                              Integer code,
                              String name,
                              String description,
                              Boolean isSystem,
                              Boolean isActive) {
        category.setLiabilityType(liabilityType);
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

    private LiabilityMainCategoryRes toResponse(LiabilityMainCategory category) {
        return new LiabilityMainCategoryRes(
                category.getId(),
                category.getLiabilityType(),
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
