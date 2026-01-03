package me.nimnakse.water_management.inventory.templates.service.impl;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.inventory.master_categories.entity.InventoryMasterCategory;
import me.nimnakse.water_management.inventory.master_categories.repository.InventoryMasterCategoryRepository;
import me.nimnakse.water_management.inventory.templates.dto.request.InventoryTemplateCreateReq;
import me.nimnakse.water_management.inventory.templates.dto.request.InventoryTemplateImportReq;
import me.nimnakse.water_management.inventory.templates.dto.request.InventoryTemplateUpdateReq;
import me.nimnakse.water_management.inventory.templates.dto.response.InventoryTemplateRes;
import me.nimnakse.water_management.inventory.templates.entity.InventoryTemplate;
import me.nimnakse.water_management.inventory.templates.entity.InventoryTemplateImport;
import me.nimnakse.water_management.inventory.templates.repository.InventoryTemplateImportRepository;
import me.nimnakse.water_management.inventory.templates.repository.InventoryTemplateRepository;
import me.nimnakse.water_management.inventory.templates.service.InventoryTemplateService;
import me.nimnakse.water_management.organization.entity.OrgUnit;
import me.nimnakse.water_management.organization.repository.OrgUnitRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryTemplateServiceImpl implements InventoryTemplateService {
    private static final String TEMPLATE_CODE_PREFIX = "IT-";
    private static final int TEMPLATE_CODE_RANDOM_LENGTH = 6;

    private final InventoryTemplateRepository templateRepository;
    private final InventoryTemplateImportRepository importRepository;
    private final InventoryMasterCategoryRepository categoryRepository;
    private final OrgUnitRepository orgUnitRepository;
    private final SecureRandom random = new SecureRandom();

    public InventoryTemplateServiceImpl(InventoryTemplateRepository templateRepository,
                                        InventoryTemplateImportRepository importRepository,
                                        InventoryMasterCategoryRepository categoryRepository,
                                        OrgUnitRepository orgUnitRepository) {
        this.templateRepository = templateRepository;
        this.importRepository = importRepository;
        this.categoryRepository = categoryRepository;
        this.orgUnitRepository = orgUnitRepository;
    }

    @Transactional
    @Override
    public InventoryTemplateRes create(InventoryTemplateCreateReq request) {
        InventoryMasterCategory levelOne = loadCategory(request.levelOneCategoryId(), 1, "Level 1 category not found");
        InventoryMasterCategory levelTwo = loadCategory(request.levelTwoCategoryId(), 2, "Level 2 category not found");
        InventoryMasterCategory levelThree = loadCategory(request.levelThreeCategoryId(), 3, "Level 3 category not found");
        validateHierarchy(levelOne, levelTwo, levelThree);

        String templateCode = resolveTemplateCode(request.templateCode(), null);
        validateCombinationUniqueness(levelOne.getId(), levelTwo.getId(), levelThree.getId(), null);

        InventoryTemplate template = new InventoryTemplate();
        template.setTemplateCode(templateCode);
        template.setLevelOneCategoryId(levelOne.getId());
        template.setLevelTwoCategoryId(levelTwo.getId());
        template.setLevelThreeCategoryId(levelThree.getId());

        InventoryTemplate saved = templateRepository.save(template);
        return toResponse(saved, Map.of(
                levelOne.getId(), levelOne,
                levelTwo.getId(), levelTwo,
                levelThree.getId(), levelThree
        ));
    }

    @Transactional
    @Override
    public InventoryTemplateRes update(Long id, InventoryTemplateUpdateReq request) {
        InventoryTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inventory template not found", ErrorCode.NOT_FOUND));

        InventoryMasterCategory levelOne = loadCategory(request.levelOneCategoryId(), 1, "Level 1 category not found");
        InventoryMasterCategory levelTwo = loadCategory(request.levelTwoCategoryId(), 2, "Level 2 category not found");
        InventoryMasterCategory levelThree = loadCategory(request.levelThreeCategoryId(), 3, "Level 3 category not found");
        validateHierarchy(levelOne, levelTwo, levelThree);

        String templateCode = resolveTemplateCode(request.templateCode(), id);
        validateCombinationUniqueness(levelOne.getId(), levelTwo.getId(), levelThree.getId(), id);

        template.setTemplateCode(templateCode);
        template.setLevelOneCategoryId(levelOne.getId());
        template.setLevelTwoCategoryId(levelTwo.getId());
        template.setLevelThreeCategoryId(levelThree.getId());

        InventoryTemplate saved = templateRepository.save(template);
        return toResponse(saved, Map.of(
                levelOne.getId(), levelOne,
                levelTwo.getId(), levelTwo,
                levelThree.getId(), levelThree
        ));
    }

    @Transactional(readOnly = true)
    @Override
    public InventoryTemplateRes getById(Long id) {
        InventoryTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inventory template not found", ErrorCode.NOT_FOUND));
        return toResponse(template, loadCategoriesMap(List.of(template)));
    }

    @Transactional
    @Override
    public void createIfMissing(Long levelOneCategoryId, Long levelTwoCategoryId, Long levelThreeCategoryId) {
        boolean exists = templateRepository.existsByLevelOneCategoryIdAndLevelTwoCategoryIdAndLevelThreeCategoryId(
                levelOneCategoryId, levelTwoCategoryId, levelThreeCategoryId);
        if (!exists) {
            create(new InventoryTemplateCreateReq(levelOneCategoryId, levelTwoCategoryId, levelThreeCategoryId, null));
        }
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<InventoryTemplateRes> getPage(int page, int size) {
        Pageable pageable = buildPageable(page, size);
        Page<InventoryTemplate> templates = templateRepository.findAll(pageable);
        Map<Long, InventoryMasterCategory> categories = loadCategoriesMap(templates.getContent());
        List<InventoryTemplateRes> content = templates.getContent().stream()
                .map(template -> toResponse(template, categories))
                .toList();
        return new PageResponse<>(
                content,
                templates.getTotalElements(),
                templates.getTotalPages(),
                templates.getNumber(),
                templates.getSize()
        );
    }

    @Transactional
    @Override
    public void delete(Long id) {
        InventoryTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inventory template not found", ErrorCode.NOT_FOUND));
        templateRepository.delete(template);
    }

    @Transactional
    @Override
    public List<InventoryTemplateRes> importTemplates(InventoryTemplateImportReq request) {
        OrgUnit orgUnit = orgUnitRepository.findById(request.orgUnitId())
                .orElseThrow(() -> new NotFoundException("Org unit not found", ErrorCode.NOT_FOUND));

        if (request.templateIds().isEmpty()) {
            throw new BadRequestException("At least one template id must be provided");
        }

        List<InventoryTemplate> templates = templateRepository.findByIdIn(request.templateIds());
        Set<Long> foundIds = templates.stream().map(InventoryTemplate::getId).collect(Collectors.toSet());
        List<Long> missing = request.templateIds().stream()
                .filter(id -> !foundIds.contains(id))
                .toList();
        if (!missing.isEmpty()) {
            throw new NotFoundException("One or more inventory templates were not found", ErrorCode.NOT_FOUND);
        }

        for (InventoryTemplate template : templates) {
            if (!importRepository.existsByOrgUnitIdAndTemplateId(orgUnit.getId(), template.getId())) {
                InventoryTemplateImport mapping = new InventoryTemplateImport();
                mapping.setOrgUnitId(orgUnit.getId());
                mapping.setTemplateId(template.getId());
                importRepository.save(mapping);
            }
        }

        Map<Long, InventoryMasterCategory> categories = loadCategoriesMap(templates);
        return templates.stream()
                .map(template -> toResponse(template, categories))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<InventoryTemplateRes> getByOrgUnit(Long orgUnitId, int page, int size) {
        OrgUnit orgUnit = orgUnitRepository.findById(orgUnitId)
                .orElseThrow(() -> new NotFoundException("Org unit not found", ErrorCode.NOT_FOUND));

        Pageable pageable = buildPageable(page, size);
        Page<InventoryTemplate> templatesPage = templateRepository.findAvailableForOrgUnit(
                orgUnit.getId(), pageable);

        Map<Long, InventoryMasterCategory> categories = loadCategoriesMap(templatesPage.getContent());
        List<InventoryTemplateRes> content = templatesPage.getContent().stream()
                .map(template -> toResponse(template, categories))
                .toList();

        return new PageResponse<>(
                content,
                templatesPage.getTotalElements(),
                templatesPage.getTotalPages(),
                templatesPage.getNumber(),
                templatesPage.getSize()
        );
    }

    private InventoryTemplateRes toResponse(InventoryTemplate template, Map<Long, InventoryMasterCategory> categories) {
        InventoryMasterCategory levelOne = categories.get(template.getLevelOneCategoryId());
        InventoryMasterCategory levelTwo = categories.get(template.getLevelTwoCategoryId());
        InventoryMasterCategory levelThree = categories.get(template.getLevelThreeCategoryId());

        return new InventoryTemplateRes(
                template.getId(),
                template.getTemplateCode(),
                template.getLevelOneCategoryId(),
                levelOne != null ? levelOne.getName() : null,
                template.getLevelTwoCategoryId(),
                levelTwo != null ? levelTwo.getName() : null,
                levelTwo != null ? levelTwo.getSpecification01() : null,
                levelTwo != null ? levelTwo.getSpecification02() : null,
                template.getLevelThreeCategoryId(),
                levelThree != null ? levelThree.getName() : null,
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }

    private Map<Long, InventoryMasterCategory> loadCategoriesMap(List<InventoryTemplate> templates) {
        Set<Long> categoryIds = new HashSet<>();
        for (InventoryTemplate template : templates) {
            categoryIds.add(template.getLevelOneCategoryId());
            categoryIds.add(template.getLevelTwoCategoryId());
            categoryIds.add(template.getLevelThreeCategoryId());
        }
        if (categoryIds.isEmpty()) {
            return Map.of();
        }

        List<InventoryMasterCategory> categories = categoryRepository.findAllById(categoryIds);
        Map<Long, InventoryMasterCategory> categoryMap = new HashMap<>();
        for (InventoryMasterCategory category : categories) {
            categoryMap.put(category.getId(), category);
        }
        return categoryMap;
    }

    private void validateHierarchy(InventoryMasterCategory levelOne, InventoryMasterCategory levelTwo,
                                   InventoryMasterCategory levelThree) {
        if (!Objects.equals(levelTwo.getParentId(), levelOne.getId())) {
            throw new BadRequestException("Level 2 category must belong to the provided level 1 category");
        }
        if (!Objects.equals(levelThree.getParentId(), levelTwo.getId())) {
            throw new BadRequestException("Level 3 category must belong to the provided level 2 category");
        }
    }

    private InventoryMasterCategory loadCategory(Long categoryId, int expectedLevel, String notFoundMessage) {
        InventoryMasterCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(notFoundMessage, ErrorCode.NOT_FOUND));
        if (category.getLevel() != expectedLevel) {
            throw new BadRequestException(notFoundMessage);
        }
        return category;
    }

    private String resolveTemplateCode(String inputCode, Long currentId) {
        String normalized = normalizeCode(inputCode);
        if (normalized == null) {
            normalized = generateTemplateCode();
        }

        boolean exists = currentId == null
                ? templateRepository.existsByTemplateCodeIgnoreCase(normalized)
                : templateRepository.existsByTemplateCodeIgnoreCaseAndIdNot(normalized, currentId);
        if (exists) {
            throw new BadRequestException("Inventory template code must be unique");
        }
        return normalized;
    }

    private void validateCombinationUniqueness(Long levelOneId, Long levelTwoId, Long levelThreeId, Long currentId) {
        boolean exists = currentId == null
                ? templateRepository.existsByLevelOneCategoryIdAndLevelTwoCategoryIdAndLevelThreeCategoryId(levelOneId, levelTwoId, levelThreeId)
                : templateRepository.existsByLevelOneCategoryIdAndLevelTwoCategoryIdAndLevelThreeCategoryIdAndIdNot(levelOneId, levelTwoId, levelThreeId, currentId);
        if (exists) {
            throw new BadRequestException("Inventory template already exists for the provided category combination");
        }
    }

    private String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        String trimmed = code.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toUpperCase();
    }

    private String generateTemplateCode() {
        String code;
        do {
            code = TEMPLATE_CODE_PREFIX + randomNumericString(TEMPLATE_CODE_RANDOM_LENGTH);
        } while (templateRepository.existsByTemplateCodeIgnoreCase(code));
        return code;
    }

    private String randomNumericString(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(random.nextInt(10));
        }
        return builder.toString();
    }

    private Pageable buildPageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : size;
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
