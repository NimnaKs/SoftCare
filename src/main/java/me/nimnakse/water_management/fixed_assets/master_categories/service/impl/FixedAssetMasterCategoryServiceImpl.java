package me.nimnakse.water_management.fixed_assets.master_categories.service.impl;

import java.util.Comparator;
import java.util.List;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.fixed_assets.master_categories.dto.request.FixedAssetMasterCategoryCreateReq;
import me.nimnakse.water_management.fixed_assets.master_categories.dto.request.FixedAssetMasterCategoryUpdateReq;
import me.nimnakse.water_management.fixed_assets.master_categories.dto.response.FixedAssetMasterCategoryRes;
import me.nimnakse.water_management.fixed_assets.master_categories.entity.FixedAssetMasterCategory;
import me.nimnakse.water_management.fixed_assets.master_categories.repository.FixedAssetMasterCategoryRepository;
import me.nimnakse.water_management.fixed_assets.master_categories.service.FixedAssetMasterCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FixedAssetMasterCategoryServiceImpl implements FixedAssetMasterCategoryService {
    private final FixedAssetMasterCategoryRepository repository;

    public FixedAssetMasterCategoryServiceImpl(FixedAssetMasterCategoryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @Override
    public FixedAssetMasterCategoryRes create(FixedAssetMasterCategoryCreateReq request) {
        validateParent(request.parentId(), request.level(), null);
        validateUniqueness(request.level(), request.name(), request.parentId(), null);
        FixedAssetMasterCategory category = new FixedAssetMasterCategory();
        applyRequest(category,
                request.parentId(),
                request.level(),
                request.name(),
                request.specification01(),
                request.specification02(),
                request.unit(),
                request.isLeaf(),
                request.isSystem(),
                request.isActive());
        return toResponse(repository.save(category));
    }

    @Transactional
    @Override
    public FixedAssetMasterCategoryRes update(Long id, FixedAssetMasterCategoryUpdateReq request) {
        FixedAssetMasterCategory category = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fixed asset category not found", ErrorCode.NOT_FOUND));
        validateParent(request.parentId(), request.level(), id);
        validateUniqueness(request.level(), request.name(), request.parentId(), id);
        applyRequest(category,
                request.parentId(),
                request.level(),
                request.name(),
                request.specification01(),
                request.specification02(),
                request.unit(),
                request.isLeaf(),
                request.isSystem(),
                request.isActive());
        return toResponse(repository.save(category));
    }

    @Transactional(readOnly = true)
    @Override
    public FixedAssetMasterCategoryRes getById(Long id) {
        FixedAssetMasterCategory category = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fixed asset category not found", ErrorCode.NOT_FOUND));
        return toResponse(category);
    }

    @Transactional(readOnly = true)
    @Override
    public List<FixedAssetMasterCategoryRes> list(Long parentId) {
        List<FixedAssetMasterCategory> categories = parentId == null
                ? repository.findAll()
                : repository.findByParentId(parentId);
        return categories.stream()
                .sorted(Comparator.comparing(FixedAssetMasterCategory::getLevel)
                        .thenComparing(FixedAssetMasterCategory::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long id) {
        FixedAssetMasterCategory category = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fixed asset category not found", ErrorCode.NOT_FOUND));
        if (Boolean.TRUE.equals(category.getIsSystem())) {
            throw new BadRequestException("System fixed asset categories cannot be deleted");
        }
        if (repository.existsByParentId(id)) {
            throw new BadRequestException("Category has child categories and cannot be deleted");
        }
        repository.delete(category);
    }

    private void validateParent(Long parentId, Integer level, Long currentId) {
        if (parentId == null) {
            return;
        }
        FixedAssetMasterCategory parent = repository.findById(parentId)
                .orElseThrow(() -> new NotFoundException("Parent category not found", ErrorCode.NOT_FOUND));
        if (parentId.equals(currentId)) {
            throw new BadRequestException("Category cannot reference itself as parent");
        }
        if (Boolean.TRUE.equals(parent.getIsLeaf())) {
            throw new BadRequestException("Cannot assign a leaf category as parent");
        }
        if (level != null && parent.getLevel() != null && level <= parent.getLevel()) {
            throw new BadRequestException("Child level must be greater than parent level");
        }
    }

    private void validateUniqueness(Integer level, String name, Long parentId, Long id) {
        boolean exists;
        if (parentId == null) {
            exists = id == null
                    ? repository.existsByLevelAndNameIgnoreCaseAndParentIdIsNull(level, name)
                    : repository.existsByLevelAndNameIgnoreCaseAndParentIdIsNullAndIdNot(level, name, id);
        } else {
            exists = id == null
                    ? repository.existsByLevelAndNameIgnoreCaseAndParentId(level, name, parentId)
                    : repository.existsByLevelAndNameIgnoreCaseAndParentIdAndIdNot(level, name, parentId, id);
        }
        if (exists) {
            throw new BadRequestException("Category with the same level and name already exists for the parent");
        }
    }

    private void applyRequest(FixedAssetMasterCategory category,
                              Long parentId,
                              Integer level,
                              String name,
                              String specification01,
                              String specification02,
                              String unit,
                              Boolean isLeaf,
                              Boolean isSystem,
                              Boolean isActive) {
        category.setParentId(parentId);
        category.setLevel(level);
        category.setName(name.trim());
        category.setSpecification01(trimToNull(specification01));
        category.setSpecification02(trimToNull(specification02));
        category.setUnit(trimToNull(unit));
        if (isLeaf != null) {
            category.setIsLeaf(isLeaf);
        }
        if (isSystem != null) {
            category.setIsSystem(isSystem);
        }
        if (isActive != null) {
            category.setIsActive(isActive);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private FixedAssetMasterCategoryRes toResponse(FixedAssetMasterCategory category) {
        return new FixedAssetMasterCategoryRes(
                category.getId(),
                category.getParentId(),
                category.getLevel(),
                category.getName(),
                category.getSpecification01(),
                category.getSpecification02(),
                category.getUnit(),
                category.getIsLeaf(),
                category.getIsSystem(),
                category.getIsActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
