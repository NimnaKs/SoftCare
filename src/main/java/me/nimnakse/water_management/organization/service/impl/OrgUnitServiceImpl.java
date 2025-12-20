package me.nimnakse.water_management.organization.service.impl;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.organization.dto.request.OrgUnitCreateReq;
import me.nimnakse.water_management.organization.dto.response.OrgUnitRes;
import me.nimnakse.water_management.organization.dto.response.OrgUnitTreeRes;
import me.nimnakse.water_management.organization.entity.OrgUnit;
import me.nimnakse.water_management.organization.entity.OrgUnitLevel;
import me.nimnakse.water_management.organization.repository.OrgUnitRepository;
import me.nimnakse.water_management.organization.repository.WaterProjectRepository;
import me.nimnakse.water_management.organization.service.OrgUnitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrgUnitServiceImpl implements OrgUnitService {
    private static final int MIN_LEVEL_INDEX = 0;
    private static final Map<OrgUnitLevel, Integer> LEVEL_ORDER = new EnumMap<>(OrgUnitLevel.class);

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

    private OrgUnitRes toResponse(OrgUnit orgUnit) {
        return new OrgUnitRes(orgUnit.getId(), orgUnit.getName(), orgUnit.getLevel(),
                orgUnit.getParentId(), orgUnit.getWaterProjectId());
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
}
