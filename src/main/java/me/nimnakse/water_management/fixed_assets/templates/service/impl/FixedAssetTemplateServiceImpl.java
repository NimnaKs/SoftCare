package me.nimnakse.water_management.fixed_assets.templates.service.impl;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.fixed_assets.master_categories.entity.FixedAssetMasterCategory;
import me.nimnakse.water_management.fixed_assets.master_categories.repository.FixedAssetMasterCategoryRepository;
import me.nimnakse.water_management.fixed_assets.templates.dto.request.FixedAssetTemplateCreateReq;
import me.nimnakse.water_management.fixed_assets.templates.dto.request.FixedAssetTemplateImportReq;
import me.nimnakse.water_management.fixed_assets.templates.dto.request.FixedAssetTemplateUpdateReq;
import me.nimnakse.water_management.fixed_assets.templates.dto.response.FixedAssetTemplateRes;
import me.nimnakse.water_management.fixed_assets.templates.entity.FixedAssetTemplate;
import me.nimnakse.water_management.fixed_assets.templates.entity.FixedAssetTemplateImport;
import me.nimnakse.water_management.fixed_assets.templates.repository.FixedAssetTemplateImportRepository;
import me.nimnakse.water_management.fixed_assets.templates.repository.FixedAssetTemplateRepository;
import me.nimnakse.water_management.fixed_assets.templates.service.FixedAssetTemplateService;
import me.nimnakse.water_management.organization.entity.OrgUnit;
import me.nimnakse.water_management.organization.repository.OrgUnitRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FixedAssetTemplateServiceImpl implements FixedAssetTemplateService {
    private static final String TEMPLATE_CODE_PREFIX = "FAT-";
    private static final int TEMPLATE_CODE_RANDOM_LENGTH = 6;

    private final FixedAssetTemplateRepository templateRepository;
    private final FixedAssetTemplateImportRepository importRepository;
    private final FixedAssetMasterCategoryRepository categoryRepository;
    private final OrgUnitRepository orgUnitRepository;
    private final SecureRandom random = new SecureRandom();

    public FixedAssetTemplateServiceImpl(FixedAssetTemplateRepository templateRepository,
                                         FixedAssetTemplateImportRepository importRepository,
                                         FixedAssetMasterCategoryRepository categoryRepository,
                                         OrgUnitRepository orgUnitRepository) {
        this.templateRepository = templateRepository;
        this.importRepository = importRepository;
        this.categoryRepository = categoryRepository;
        this.orgUnitRepository = orgUnitRepository;
    }

    @Transactional
    @Override
    public FixedAssetTemplateRes create(FixedAssetTemplateCreateReq request) {
        FixedAssetMasterCategory levelOne = loadCategory(request.levelOneCategoryId(), 1, "Level 1 fixed asset category not found");
        FixedAssetMasterCategory levelTwo = loadCategory(request.levelTwoCategoryId(), 2, "Level 2 fixed asset category not found");
        FixedAssetMasterCategory levelThree = loadCategory(request.levelThreeCategoryId(), 3, "Level 3 fixed asset category not found");
        validateHierarchy(levelOne, levelTwo, levelThree);

        String templateCode = resolveTemplateCode(request.templateCode(), null);
        validateCombinationUniqueness(levelOne.getId(), levelTwo.getId(), levelThree.getId(), null);

        FixedAssetTemplate template = new FixedAssetTemplate();
        template.setTemplateCode(templateCode);
        template.setLevelOneCategoryId(levelOne.getId());
        template.setLevelTwoCategoryId(levelTwo.getId());
        template.setLevelThreeCategoryId(levelThree.getId());

        FixedAssetTemplate saved = templateRepository.save(template);
        return toResponse(saved, Map.of(
                levelOne.getId(), levelOne,
                levelTwo.getId(), levelTwo,
                levelThree.getId(), levelThree
        ));
    }

    @Transactional
    @Override
    public FixedAssetTemplateRes update(Long id, FixedAssetTemplateUpdateReq request) {
        FixedAssetTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fixed asset template not found", ErrorCode.NOT_FOUND));

        FixedAssetMasterCategory levelOne = loadCategory(request.levelOneCategoryId(), 1, "Level 1 fixed asset category not found");
        FixedAssetMasterCategory levelTwo = loadCategory(request.levelTwoCategoryId(), 2, "Level 2 fixed asset category not found");
        FixedAssetMasterCategory levelThree = loadCategory(request.levelThreeCategoryId(), 3, "Level 3 fixed asset category not found");
        validateHierarchy(levelOne, levelTwo, levelThree);

        String templateCode = resolveTemplateCode(request.templateCode(), id);
        validateCombinationUniqueness(levelOne.getId(), levelTwo.getId(), levelThree.getId(), id);

        template.setTemplateCode(templateCode);
        template.setLevelOneCategoryId(levelOne.getId());
        template.setLevelTwoCategoryId(levelTwo.getId());
        template.setLevelThreeCategoryId(levelThree.getId());

        FixedAssetTemplate saved = templateRepository.save(template);
        return toResponse(saved, Map.of(
                levelOne.getId(), levelOne,
                levelTwo.getId(), levelTwo,
                levelThree.getId(), levelThree
        ));
    }

    @Transactional(readOnly = true)
    @Override
    public FixedAssetTemplateRes getById(Long id) {
        FixedAssetTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fixed asset template not found", ErrorCode.NOT_FOUND));
        return toResponse(template, loadCategoriesMap(List.of(template)));
    }

    @Transactional
    @Override
    public void createIfMissing(Long levelOneCategoryId, Long levelTwoCategoryId, Long levelThreeCategoryId) {
        boolean exists = templateRepository.existsByLevelOneCategoryIdAndLevelTwoCategoryIdAndLevelThreeCategoryId(
                levelOneCategoryId, levelTwoCategoryId, levelThreeCategoryId);
        if (!exists) {
            create(new FixedAssetTemplateCreateReq(levelOneCategoryId, levelTwoCategoryId, levelThreeCategoryId, null));
        }
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<FixedAssetTemplateRes> getPage(int page, int size) {
        Pageable pageable = buildPageable(page, size);
        Page<FixedAssetTemplate> templates = templateRepository.findAll(pageable);
        Map<Long, FixedAssetMasterCategory> categories = loadCategoriesMap(templates.getContent());
        List<FixedAssetTemplateRes> content = templates.getContent().stream()
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
        FixedAssetTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fixed asset template not found", ErrorCode.NOT_FOUND));
        templateRepository.delete(template);
    }

    @Transactional
    @Override
    public List<FixedAssetTemplateRes> importTemplates(FixedAssetTemplateImportReq request) {
        OrgUnit orgUnit = orgUnitRepository.findById(request.orgUnitId())
                .orElseThrow(() -> new NotFoundException("Org unit not found", ErrorCode.NOT_FOUND));

        if (request.templateIds().isEmpty()) {
            throw new BadRequestException("At least one template id must be provided");
        }

        List<FixedAssetTemplate> templates = templateRepository.findByIdIn(request.templateIds());
        Set<Long> foundIds = templates.stream().map(FixedAssetTemplate::getId).collect(Collectors.toSet());
        List<Long> missing = request.templateIds().stream()
                .filter(id -> !foundIds.contains(id))
                .toList();
        if (!missing.isEmpty()) {
            throw new NotFoundException("One or more fixed asset templates were not found", ErrorCode.NOT_FOUND);
        }

        for (FixedAssetTemplate template : templates) {
            if (!importRepository.existsByOrgUnitIdAndTemplateId(orgUnit.getId(), template.getId())) {
                FixedAssetTemplateImport mapping = new FixedAssetTemplateImport();
                mapping.setOrgUnitId(orgUnit.getId());
                mapping.setTemplateId(template.getId());
                importRepository.save(mapping);
            }
        }

        Map<Long, FixedAssetMasterCategory> categories = loadCategoriesMap(templates);
        return templates.stream()
                .map(template -> toResponse(template, categories))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<FixedAssetTemplateRes> getByOrgUnit(Long orgUnitId, int page, int size) {
        OrgUnit orgUnit = orgUnitRepository.findById(orgUnitId)
                .orElseThrow(() -> new NotFoundException("Org unit not found", ErrorCode.NOT_FOUND));

        Pageable pageable = buildPageable(page, size);
        List<Long> importedTemplateIds = importRepository.findByOrgUnitId(orgUnit.getId()).stream()
                .map(FixedAssetTemplateImport::getTemplateId)
                .toList();

        Page<FixedAssetTemplate> templatesPage = importedTemplateIds.isEmpty()
                ? templateRepository.findAll(pageable)
                : templateRepository.findByIdNotIn(importedTemplateIds, pageable);

        Map<Long, FixedAssetMasterCategory> categories = loadCategoriesMap(templatesPage.getContent());
        List<FixedAssetTemplateRes> content = templatesPage.getContent().stream()
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

    private FixedAssetTemplateRes toResponse(FixedAssetTemplate template, Map<Long, FixedAssetMasterCategory> categories) {
        FixedAssetMasterCategory levelOne = categories.get(template.getLevelOneCategoryId());
        FixedAssetMasterCategory levelTwo = categories.get(template.getLevelTwoCategoryId());
        FixedAssetMasterCategory levelThree = categories.get(template.getLevelThreeCategoryId());

        return new FixedAssetTemplateRes(
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

    private void validateHierarchy(FixedAssetMasterCategory levelOne,
                                   FixedAssetMasterCategory levelTwo,
                                   FixedAssetMasterCategory levelThree) {
        if (!Objects.equals(levelTwo.getParentId(), levelOne.getId())) {
            throw new BadRequestException("Level 2 category must belong to the provided level 1 category");
        }
        if (!Objects.equals(levelThree.getParentId(), levelTwo.getId())) {
            throw new BadRequestException("Level 3 category must belong to the provided level 2 category");
        }
    }

    private String resolveTemplateCode(String requestedCode, Long id) {
        if (requestedCode != null && !requestedCode.isBlank()) {
            String normalized = requestedCode.trim();
            boolean exists = id == null
                    ? templateRepository.existsByTemplateCodeIgnoreCase(normalized)
                    : templateRepository.existsByTemplateCodeIgnoreCaseAndIdNot(normalized, id);
            if (exists) {
                throw new BadRequestException("Template code already exists");
            }
            return normalized;
        }

        Set<String> attemptedCodes = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            String generated = TEMPLATE_CODE_PREFIX + generateRandomNumericCode(TEMPLATE_CODE_RANDOM_LENGTH);
            if (attemptedCodes.add(generated)
                    && !templateRepository.existsByTemplateCodeIgnoreCase(generated)) {
                return generated;
            }
        }
        throw new BadRequestException("Unable to generate a unique template code");
    }

    private String generateRandomNumericCode(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(random.nextInt(10));
        }
        return builder.toString();
    }

    private void validateCombinationUniqueness(Long levelOneId, Long levelTwoId, Long levelThreeId, Long id) {
        boolean exists = id == null
                ? templateRepository.existsByLevelOneCategoryIdAndLevelTwoCategoryIdAndLevelThreeCategoryId(
                        levelOneId, levelTwoId, levelThreeId)
                : templateRepository.existsByLevelOneCategoryIdAndLevelTwoCategoryIdAndLevelThreeCategoryIdAndIdNot(
                        levelOneId, levelTwoId, levelThreeId, id);
        if (exists) {
            throw new BadRequestException("Template for the provided category combination already exists");
        }
    }

    private Map<Long, FixedAssetMasterCategory> loadCategoriesMap(List<FixedAssetTemplate> templates) {
        Set<Long> ids = new HashSet<>();
        for (FixedAssetTemplate template : templates) {
            ids.add(template.getLevelOneCategoryId());
            ids.add(template.getLevelTwoCategoryId());
            ids.add(template.getLevelThreeCategoryId());
        }

        List<FixedAssetMasterCategory> categories = categoryRepository.findAllById(ids);
        return categories.stream()
                .collect(Collectors.toMap(FixedAssetMasterCategory::getId, Function.identity()));
    }

    private FixedAssetMasterCategory loadCategory(Long id, int expectedLevel, String notFoundMessage) {
        FixedAssetMasterCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(notFoundMessage, ErrorCode.NOT_FOUND));
        if (category.getLevel() == null || category.getLevel() != expectedLevel) {
            throw new BadRequestException("Category level mismatch for template creation");
        }
        return category;
    }

    private Pageable buildPageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : size;
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "templateCode"));
    }
}
