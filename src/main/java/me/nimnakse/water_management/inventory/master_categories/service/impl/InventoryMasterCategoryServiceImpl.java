package me.nimnakse.water_management.inventory.master_categories.service.impl;

import java.util.List;
import java.util.Objects;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.inventory.master_categories.dto.request.InventoryMasterCategoryCreateReq;
import me.nimnakse.water_management.inventory.master_categories.dto.request.InventoryMasterCategoryUpdateReq;
import me.nimnakse.water_management.inventory.master_categories.dto.response.InventoryMasterCategoryRes;
import me.nimnakse.water_management.inventory.master_categories.entity.InventoryMasterCategory;
import me.nimnakse.water_management.inventory.master_categories.repository.InventoryMasterCategoryRepository;
import me.nimnakse.water_management.inventory.master_categories.service.InventoryMasterCategoryService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryMasterCategoryServiceImpl implements InventoryMasterCategoryService {
    private final InventoryMasterCategoryRepository repository;

    public InventoryMasterCategoryServiceImpl(InventoryMasterCategoryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @Override
    public InventoryMasterCategoryRes create(InventoryMasterCategoryCreateReq request) {
        InventoryMasterCategory parent = loadParent(request.parentId());
        validateLevelAgainstParent(request.level(), parent);
        String normalizedName = normalizeName(request.name());
        validateUniqueness(request.level(), normalizedName, request.parentId(), null);

        InventoryMasterCategory category = new InventoryMasterCategory();
        applyRequest(category, parent, request.level(), normalizedName, request.specification01(),
                request.specification02(), request.unit(), request.isLeaf(), request.isSystem(), request.isActive());
        InventoryMasterCategory saved = repository.save(category);

        if (parent != null && Boolean.TRUE.equals(parent.getIsLeaf())) {
            parent.setIsLeaf(Boolean.FALSE);
            repository.save(parent);
        }

        return toResponse(saved);
    }

    @Transactional
    @Override
    public InventoryMasterCategoryRes update(Long id, InventoryMasterCategoryUpdateReq request) {
        InventoryMasterCategory category = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inventory master category not found", ErrorCode.NOT_FOUND));

        if (request.parentId() != null && request.parentId().equals(id)) {
            throw new BadRequestException("Category cannot be its own parent");
        }

        InventoryMasterCategory newParent = loadParent(request.parentId());
        validateLevelAgainstParent(request.level(), newParent);

        String normalizedName = normalizeName(request.name());
        validateUniqueness(request.level(), normalizedName, request.parentId(), id);

        boolean hasChildren = repository.existsByParentId(id);
        if (Boolean.TRUE.equals(request.isLeaf()) && hasChildren) {
            throw new BadRequestException("Cannot mark category as leaf while it has child categories");
        }

        Long previousParentId = category.getParentId();
        applyRequest(category, newParent, request.level(), normalizedName, request.specification01(),
                request.specification02(), request.unit(), request.isLeaf(), request.isSystem(), request.isActive());
        InventoryMasterCategory saved = repository.save(category);

        updateParentLeafStatus(newParent);
        if (!Objects.equals(previousParentId, request.parentId())) {
            updateParentLeafStatus(loadParent(previousParentId));
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public InventoryMasterCategoryRes getById(Long id) {
        InventoryMasterCategory category = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inventory master category not found", ErrorCode.NOT_FOUND));
        return toResponse(category);
    }

    @Transactional(readOnly = true)
    @Override
    public List<InventoryMasterCategoryRes> list() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "level", "name")).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long id) {
        InventoryMasterCategory category = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inventory master category not found", ErrorCode.NOT_FOUND));
        if (repository.existsByParentId(id)) {
            throw new BadRequestException("Cannot delete category with child categories");
        }

        repository.delete(category);
        updateParentLeafStatus(loadParent(category.getParentId()));
    }

    private void validateUniqueness(Integer level, String name, Long parentId, Long id) {
        boolean exists;
        if (id == null) {
            exists = repository.existsByLevelAndNameIgnoreCaseAndParentId(level, name, parentId);
        } else {
            exists = repository.existsByLevelAndNameIgnoreCaseAndParentIdAndIdNot(level, name, parentId, id);
        }
        if (exists) {
            throw new BadRequestException("Inventory master category already exists for the given level and parent");
        }
    }

    private InventoryMasterCategory loadParent(Long parentId) {
        if (parentId == null) {
            return null;
        }
        return repository.findById(parentId)
                .orElseThrow(() -> new NotFoundException("Parent category not found", ErrorCode.NOT_FOUND));
    }

    private void validateLevelAgainstParent(Integer level, InventoryMasterCategory parent) {
        if (parent != null && level <= parent.getLevel()) {
            throw new BadRequestException("Child category level must be greater than parent level");
        }
    }

    private String normalizeName(String name) {
        return name == null ? null : name.trim();
    }

    private void applyRequest(InventoryMasterCategory category,
                              InventoryMasterCategory parent,
                              Integer level,
                              String name,
                              String specification01,
                              String specification02,
                              String unit,
                              Boolean isLeaf,
                              Boolean isSystem,
                              Boolean isActive) {
        category.setParentId(parent != null ? parent.getId() : null);
        category.setLevel(level);
        category.setName(name);
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

    private InventoryMasterCategoryRes toResponse(InventoryMasterCategory category) {
        return new InventoryMasterCategoryRes(
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

    private void updateParentLeafStatus(InventoryMasterCategory parent) {
        if (parent == null) {
            return;
        }
        boolean hasChildren = repository.existsByParentId(parent.getId());
        boolean updatedLeaf = !hasChildren;
        if (!Objects.equals(parent.getIsLeaf(), updatedLeaf)) {
            parent.setIsLeaf(updatedLeaf);
            repository.save(parent);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
