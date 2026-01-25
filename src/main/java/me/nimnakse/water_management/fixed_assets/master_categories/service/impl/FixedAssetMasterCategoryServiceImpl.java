package me.nimnakse.water_management.fixed_assets.master_categories.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.fixed_assets.master_categories.dto.request.FixedAssetMasterCategoryCreateReq;
import me.nimnakse.water_management.fixed_assets.master_categories.dto.request.FixedAssetMasterCategoryUpdateReq;
import me.nimnakse.water_management.fixed_assets.master_categories.dto.response.FixedAssetMasterCategoryRes;
import me.nimnakse.water_management.fixed_assets.master_categories.dto.response.FixedAssetMasterCategoryTreeRes;
import me.nimnakse.water_management.fixed_assets.master_categories.entity.FixedAssetMasterCategory;
import me.nimnakse.water_management.fixed_assets.master_categories.repository.FixedAssetMasterCategoryRepository;
import me.nimnakse.water_management.fixed_assets.master_categories.service.FixedAssetMasterCategoryService;
import me.nimnakse.water_management.fixed_assets.templates.service.FixedAssetTemplateService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FixedAssetMasterCategoryServiceImpl implements FixedAssetMasterCategoryService {
    private final FixedAssetMasterCategoryRepository repository;
    private final FixedAssetTemplateService fixedAssetTemplateService;

    public FixedAssetMasterCategoryServiceImpl(FixedAssetMasterCategoryRepository repository,
            FixedAssetTemplateService fixedAssetTemplateService) {
        this.repository = repository;
        this.fixedAssetTemplateService = fixedAssetTemplateService;
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
        FixedAssetMasterCategory saved = repository.save(category);
        createFixedAssetTemplateIfNeeded(saved);
        return toResponse(saved);
    }

    @Transactional
    @Override
    public FixedAssetMasterCategoryRes update(Long id, FixedAssetMasterCategoryUpdateReq request) {
        FixedAssetMasterCategory category = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fixed asset category not found",
                        "ස්ථාවර වත්කම් ප්‍රභේදය සොයාගත නොහැක", ErrorCode.NOT_FOUND));
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
        FixedAssetMasterCategory saved = repository.save(category);
        createFixedAssetTemplateIfNeeded(saved);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public FixedAssetMasterCategoryRes getById(Long id) {
        FixedAssetMasterCategory category = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fixed asset category not found",
                        "ස්ථාවර වත්කම් ප්‍රභේදය සොයාගත නොහැක", ErrorCode.NOT_FOUND));
        return toResponse(category);
    }

    @Transactional(readOnly = true)
    @Override
    public List<FixedAssetMasterCategoryRes> list(Long parentId) {
        List<FixedAssetMasterCategory> categories;
        if (parentId == null) {
            categories = repository.findAll(Sort.by(Sort.Direction.ASC, "level", "name"));
        } else {
            categories = repository.findByParentId(parentId).stream()
                    .sorted(Comparator.comparing(FixedAssetMasterCategory::getLevel)
                            .thenComparing(FixedAssetMasterCategory::getName, String.CASE_INSENSITIVE_ORDER))
                    .toList();
        }

        return categories.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<FixedAssetMasterCategoryTreeRes> getTree() {
        List<FixedAssetMasterCategory> categories = repository.findAll(Sort.by(Sort.Direction.ASC, "level", "name"));
        Map<Long, FixedAssetMasterCategoryTreeRes> nodes = new LinkedHashMap<>();
        for (FixedAssetMasterCategory category : categories) {
            String specification01 = category.getLevel() == 2 ? category.getSpecification01() : null;
            String specification02 = category.getLevel() == 2 ? category.getSpecification02() : null;
            String unit = category.getLevel() == 2 ? category.getUnit() : null;
            nodes.put(category.getId(), new FixedAssetMasterCategoryTreeRes(
                    category.getId(),
                    category.getParentId(),
                    category.getLevel(),
                    category.getName(),
                    specification01,
                    specification02,
                    unit,
                    category.getIsSystem(),
                    category.getIsActive(),
                    new ArrayList<>()));
        }

        List<FixedAssetMasterCategoryTreeRes> roots = new ArrayList<>();
        for (FixedAssetMasterCategory category : categories) {
            FixedAssetMasterCategoryTreeRes node = nodes.get(category.getId());
            if (category.getParentId() != null && nodes.containsKey(category.getParentId())) {
                nodes.get(category.getParentId()).children().add(node);
            } else {
                roots.add(node);
            }
        }
        return roots;
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<FixedAssetMasterCategoryRes> getLevelOneCategories(int page, int size) {
        Pageable pageable = buildPageable(page, size);
        Page<FixedAssetMasterCategory> categories = repository.findByLevelAndParentIdIsNull(1, pageable);
        return toPageResponse(categories);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<FixedAssetMasterCategoryRes> getLevelTwoCategories(Long levelOneCategoryId, int page,
            int size) {
        FixedAssetMasterCategory parent = loadCategory(levelOneCategoryId);
        validateLevel(parent, 1, "Level 1 fixed asset category not found",
                "මට්ටම 1 ස්ථාවර වත්කම් ප්‍රභේදය සොයාගත නොහැක");
        Pageable pageable = buildPageable(page, size);
        Page<FixedAssetMasterCategory> categories = repository.findByLevelAndParentId(2, parent.getId(), pageable);
        return toPageResponse(categories);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<FixedAssetMasterCategoryRes> getLevelThreeCategories(Long levelTwoCategoryId, int page,
            int size) {
        FixedAssetMasterCategory parent = loadCategory(levelTwoCategoryId);
        validateLevel(parent, 2, "Level 2 fixed asset category not found",
                "මට්ටම 2 ස්ථාවර වත්කම් ප්‍රභේදය සොයාගත නොහැක");
        Pageable pageable = buildPageable(page, size);
        Page<FixedAssetMasterCategory> categories = repository.findByLevelAndParentId(3, parent.getId(), pageable);
        return toPageResponse(categories);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        FixedAssetMasterCategory category = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fixed asset category not found",
                        "ස්ථාවර වත්කම් ප්‍රභේදය සොයාගත නොහැක", ErrorCode.NOT_FOUND));
        if (Boolean.TRUE.equals(category.getIsSystem())) {
            throw new BadRequestException("System fixed asset categories cannot be deleted",
                    "පද්ධති ස්ථාවර වත්කම් ප්‍රභේද මකා දැමිය නොහැක");
        }
        if (repository.existsByParentId(id)) {
            throw new BadRequestException("Category has child categories and cannot be deleted",
                    "අනු ප්‍රභේද පවතින ප්‍රභේද මකා දැමිය නොහැක");
        }
        repository.delete(category);
    }

    private void validateParent(Long parentId, Integer level, Long currentId) {
        if (parentId == null) {
            return;
        }
        FixedAssetMasterCategory parent = repository.findById(parentId)
                .orElseThrow(() -> new NotFoundException("Fixed asset category not found",
                        "ස්ථාවර වත්කම් ප්‍රභේදය සොයාගත නොහැක", ErrorCode.NOT_FOUND));
        if (parentId.equals(currentId)) {
            throw new BadRequestException("Category cannot reference itself as parent",
                    "ප්‍රභේදය එහිම දෙමාපිය ප්‍රභේදයක් ලෙස යොමු කළ නොහැක");
        }
        if (Boolean.TRUE.equals(parent.getIsLeaf())) {
            throw new BadRequestException("Cannot assign a leaf category as parent",
                    "පත්‍ර ප්‍රභේදයක් දෙමාපිය ප්‍රභේදයක් ලෙස පවරනු ලැබිය නොහැක");
        }
        if (level != null && parent.getLevel() != null && level <= parent.getLevel()) {
            throw new BadRequestException("Child level must be greater than parent level",
                    "අනු ප්‍රභේද මට්ටම දෙමාපිය ප්‍රභේද මට්ටමට වඩා වැඩි විය යුතුය");
        }
    }

    private void createFixedAssetTemplateIfNeeded(FixedAssetMasterCategory category) {
        if (category.getLevel() == null || category.getLevel() != 3 || category.getParentId() == null) {
            return;
        }

        FixedAssetMasterCategory levelTwo = repository.findById(category.getParentId())
                .orElseThrow(() -> new NotFoundException("Fixed asset template not found",
                        "ස්ථාවර වත්කම් සැකිල්ල සොයාගත නොහැක", ErrorCode.NOT_FOUND));
        if (levelTwo.getParentId() == null) {
            throw new BadRequestException("Level 2 fixed asset category must have a level 1 parent",
                    "මට්ටම 2 ස්ථාවර වත්කම් ප්‍රභේදයකට මට්ටම 1 දෙමාපිය ප්‍රභේදයක් තිබිය යුතුය");
        }

        FixedAssetMasterCategory levelOne = repository.findById(levelTwo.getParentId())
                .orElseThrow(
                        () -> new NotFoundException("Level 1 fixed asset category not found",
                                "මට්ටම 1 ස්ථාවර වත්කම් ප්‍රභේදය සොයාගත නොහැක", ErrorCode.NOT_FOUND));

        validateLevel(levelOne, 1, "Level 1 fixed asset category not found",
                "මට්ටම 1 ස්ථාවර වත්කම් ප්‍රභේදය සොයාගත නොහැක");
        validateLevel(levelTwo, 2, "Level 2 fixed asset category not found",
                "මට්ටම 2 ස්ථාවර වත්කම් ප්‍රභේදය සොයාගත නොහැක");

        fixedAssetTemplateService.createIfMissing(levelOne.getId(), levelTwo.getId(), category.getId());
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
            throw new BadRequestException("Category with the same level and name already exists for the parent",
                    "දෙමාපිය ප්‍රභේදය සඳහා එම මට්ටමේම සහ එම නමින්ම ප්‍රභේදයක් දැනටමත් පවතී");
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

    private Pageable buildPageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : size;
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "name"));
    }

    private FixedAssetMasterCategory loadCategory(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fixed asset category not found",
                        "ස්ථාවර වත්කම් ප්‍රභේදය සොයාගත නොහැක", ErrorCode.NOT_FOUND));
    }

    private void validateLevel(FixedAssetMasterCategory category, int expectedLevel, String messageEn,
            String messageSn) {
        if (category.getLevel() != expectedLevel) {
            throw new BadRequestException(messageEn, messageSn);
        }
    }

    private PageResponse<FixedAssetMasterCategoryRes> toPageResponse(Page<FixedAssetMasterCategory> page) {
        return new PageResponse<>(
                page.getContent().stream().map(this::toResponse).toList(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize());
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
                category.getUpdatedAt());
    }
}
