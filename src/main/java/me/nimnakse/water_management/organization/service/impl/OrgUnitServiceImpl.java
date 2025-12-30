package me.nimnakse.water_management.organization.service.impl;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.organization.dto.request.OrgUnitCreateReq;
import me.nimnakse.water_management.organization.dto.request.OrgUnitUpdateReq;
import me.nimnakse.water_management.organization.dto.response.OrgUnitRes;
import me.nimnakse.water_management.organization.dto.response.OrgUnitTreeRes;
import me.nimnakse.water_management.organization.entity.OrgUnit;
import me.nimnakse.water_management.organization.entity.OrgUnitLevel;
import me.nimnakse.water_management.organization.repository.OrgUnitRepository;
import me.nimnakse.water_management.organization.repository.WaterProjectRepository;
import me.nimnakse.water_management.organization.service.OrgUnitService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class OrgUnitServiceImpl implements OrgUnitService {
    private static final int MIN_LEVEL_INDEX = 0;
    private static final Map<OrgUnitLevel, Integer> LEVEL_ORDER = new EnumMap<>(OrgUnitLevel.class);
    private static final Pattern ORG_CODE_PATTERN = Pattern.compile("^\\d{6}$");
    private static final int BRANCH_CODE_START = 400117;
    private static final int RESERVED_START = 400101;
    private static final int RESERVED_END = 400116;


    static {
        LEVEL_ORDER.put(OrgUnitLevel.NATIONAL, 0);
        LEVEL_ORDER.put(OrgUnitLevel.PROVINCE, 1);
        LEVEL_ORDER.put(OrgUnitLevel.DISTRICT, 2);
        LEVEL_ORDER.put(OrgUnitLevel.DIVISION, 3);
        LEVEL_ORDER.put(OrgUnitLevel.BRANCH, 4);
    }

    private final OrgUnitRepository orgUnitRepository;
    private final WaterProjectRepository waterProjectRepository;

    public OrgUnitServiceImpl(OrgUnitRepository orgUnitRepository,
                              WaterProjectRepository waterProjectRepository) {
        this.orgUnitRepository = orgUnitRepository;
        this.waterProjectRepository = waterProjectRepository;
    }

    @Transactional
    @Override
    public OrgUnitRes create(OrgUnitCreateReq request) {
        validateParent(request.level(), request.parentId());
        if (request.waterProjectId() != null && !waterProjectRepository.existsById(request.waterProjectId())) {
            throw new NotFoundException("Water project not found", ErrorCode.NOT_FOUND);
        }

        OrgUnit orgUnit = new OrgUnit();
        orgUnit.setName(request.name());
        orgUnit.setLevel(request.level());
        orgUnit.setParentId(request.parentId());
        orgUnit.setWaterProjectId(request.waterProjectId());

        if (request.level() == OrgUnitLevel.BRANCH) {

            if (StringUtils.hasText(request.organizationCode())) {
                // manual → validation only
                validateOrganizationCode(request.organizationCode().trim());
                orgUnit.setOrganizationCode(request.organizationCode().trim());
            } else {
                // auto → always >= 400117
                orgUnit.setOrganizationCode(generateOrganizationCode());
            }

        }


        OrgUnit saved = orgUnitRepository.save(orgUnit);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrgUnitTreeRes> getTree(Long waterProjectId) {
        List<OrgUnit> units = waterProjectId == null
                ? orgUnitRepository.findAll()
                : orgUnitRepository.findByWaterProjectId(waterProjectId);
        Map<Long, OrgUnitTreeRes> nodes = new LinkedHashMap<>();
        for (OrgUnit unit : units) {
            nodes.put(unit.getId(), new OrgUnitTreeRes(unit.getId(), unit.getName(), unit.getLevel(),
                    unit.getParentId(), unit.getWaterProjectId(), new ArrayList<>()));
        }

        List<OrgUnitTreeRes> roots = new ArrayList<>();
        for (OrgUnit unit : units) {
            OrgUnitTreeRes node = nodes.get(unit.getId());
            if (unit.getParentId() != null && nodes.containsKey(unit.getParentId())) {
                nodes.get(unit.getParentId()).children().add(node);
            } else {
                roots.add(node);
            }
        }
        return roots;
    }

    @Transactional(readOnly = true)
    @Override
    public OrgUnitRes getById(Long id) {
        OrgUnit orgUnit = orgUnitRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Org unit not found", ErrorCode.NOT_FOUND));
        return toResponse(orgUnit);
    }

    @Transactional
    @Override
    public OrgUnitRes update(Long id, OrgUnitUpdateReq request) {
        OrgUnit orgUnit = orgUnitRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Org unit not found", ErrorCode.NOT_FOUND));
        validateParent(request.level(), request.parentId());
        if (request.waterProjectId() != null && !waterProjectRepository.existsById(request.waterProjectId())) {
            throw new NotFoundException("Water project not found", ErrorCode.NOT_FOUND);
        }

        orgUnit.setName(request.name());
        orgUnit.setLevel(request.level());
        orgUnit.setParentId(request.parentId());
        orgUnit.setWaterProjectId(request.waterProjectId());

        if (orgUnit.getLevel() == OrgUnitLevel.BRANCH &&
                StringUtils.hasText(request.organizationCode())) {

            int code = Integer.parseInt(request.organizationCode());

            if (code >= RESERVED_START && code <= RESERVED_END) {
                // allowed – manual correction
                validateOrganizationCode(request.organizationCode());
                orgUnit.setOrganizationCode(request.organizationCode());
            } else {
                validateOrganizationCode(request.organizationCode());
                orgUnit.setOrganizationCode(request.organizationCode());
            }
        }


        OrgUnit saved = orgUnitRepository.save(orgUnit);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<OrgUnitRes> getLevelOneUnits(int page, int size) {
        return buildPageResponse(orgUnitRepository.findByLevel(OrgUnitLevel.NATIONAL, pageRequest(page, size)));
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<OrgUnitRes> getLevelTwoUnits(Long levelOneId, int page, int size) {
        OrgUnit parent = assertParentLevel(levelOneId, OrgUnitLevel.NATIONAL);
        return buildPageResponse(orgUnitRepository.findByLevelAndParentId(OrgUnitLevel.PROVINCE, parent.getId(),
                pageRequest(page, size)));
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<OrgUnitRes> getLevelThreeUnits(Long levelTwoId, int page, int size) {
        OrgUnit parent = assertParentLevel(levelTwoId, OrgUnitLevel.PROVINCE);
        return buildPageResponse(orgUnitRepository.findByLevelAndParentId(OrgUnitLevel.DISTRICT, parent.getId(),
                pageRequest(page, size)));
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<OrgUnitRes> getLevelFourUnits(Long levelThreeId, int page, int size) {
        OrgUnit parent = assertParentLevel(levelThreeId, OrgUnitLevel.DISTRICT);
        return buildPageResponse(orgUnitRepository.findByLevelAndParentId(OrgUnitLevel.DIVISION, parent.getId(),
                pageRequest(page, size)));
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<OrgUnitRes> getLevelFiveUnits(Long levelFourId, int page, int size) {
        OrgUnit parent = assertParentLevel(levelFourId, OrgUnitLevel.DIVISION);
        return buildPageResponse(orgUnitRepository.findByLevelAndParentId(OrgUnitLevel.BRANCH, parent.getId(),
                pageRequest(page, size)));
    }

    private OrgUnitRes toResponse(OrgUnit orgUnit) {
        return new OrgUnitRes(orgUnit.getId(), orgUnit.getName(), orgUnit.getLevel(),
                orgUnit.getParentId(), orgUnit.getWaterProjectId(),
                (orgUnit.getOrganizationCode() != null)?orgUnit.getOrganizationCode():null
        );
    }

    private void validateParent(OrgUnitLevel level, Long parentId) {
        if (level == OrgUnitLevel.NATIONAL && parentId != null) {
            throw new BadRequestException("National level org unit cannot have a parent");
        }
        if (level != OrgUnitLevel.NATIONAL && parentId == null) {
            throw new BadRequestException("Parent org unit is required");
        }
        if (parentId == null) {
            return;
        }
        OrgUnit parent = orgUnitRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException("Parent org unit not found", ErrorCode.NOT_FOUND));
        int childLevel = LEVEL_ORDER.getOrDefault(level, MIN_LEVEL_INDEX);
        int parentLevel = LEVEL_ORDER.getOrDefault(parent.getLevel(), MIN_LEVEL_INDEX);
        if (parentLevel != childLevel - 1) {
            throw new BadRequestException("Parent org unit level does not match hierarchy");
        }
    }

    private OrgUnit assertParentLevel(Long parentId, OrgUnitLevel expectedLevel) {
        OrgUnit parent = orgUnitRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException("Parent org unit not found", ErrorCode.NOT_FOUND));
        if (parent.getLevel() != expectedLevel) {
            throw new BadRequestException("Parent org unit level does not match hierarchy");
        }
        return parent;
    }

    private PageResponse<OrgUnitRes> buildPageResponse(Page<OrgUnit> page) {
        List<OrgUnitRes> items = page.getContent().stream()
                .map(this::toResponse)
                .toList();
        return new PageResponse<>(items, page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize());
    }

    private PageRequest pageRequest(int page, int size) {
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
    }

    private void validateOrganizationCode(String code) {

        if (!ORG_CODE_PATTERN.matcher(code).matches()) {
            throw new IllegalArgumentException("Organization code must be exactly 6 digits");
        }

        int numeric = Integer.parseInt(code);

        // allow reserved range ONLY for manual input
        if (numeric < RESERVED_START) {
            throw new IllegalArgumentException(
                    "Organization code must be >= " + RESERVED_START
            );
        }

        if (orgUnitRepository.existsByOrganizationCode(code)) {
            throw new IllegalArgumentException("Organization code already exists");
        }
    }

    private String generateOrganizationCode() {

        String maxCodeStr =
                orgUnitRepository.findMaxOrganizationCodeByLevel(OrgUnitLevel.BRANCH);

        int next = BRANCH_CODE_START;

        if (maxCodeStr != null && !maxCodeStr.isBlank()) {
            int max = Integer.parseInt(maxCodeStr);

            // ensure we never fall into reserved range
            next = Math.max(max + 1, BRANCH_CODE_START);
        }

        return String.valueOf(next); // ex: 400117, 400118
    }

}
