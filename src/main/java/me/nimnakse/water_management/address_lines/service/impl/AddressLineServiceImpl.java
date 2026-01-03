package me.nimnakse.water_management.address_lines.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import me.nimnakse.water_management.address_lines.dto.request.AddressLineCreateReq;
import me.nimnakse.water_management.address_lines.dto.request.AddressLineUpdateReq;
import me.nimnakse.water_management.address_lines.dto.response.AddressLineHierarchyRes;
import me.nimnakse.water_management.address_lines.dto.response.AddressLineRes;
import me.nimnakse.water_management.address_lines.entity.AddressLine;
import me.nimnakse.water_management.address_lines.repository.AddressLineRepository;
import me.nimnakse.water_management.address_lines.service.AddressLineService;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.organization.repository.OrgUnitRepository;
import me.nimnakse.water_management.security.OrganizationAccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddressLineServiceImpl implements AddressLineService {
    private final AddressLineRepository addressLineRepository;
    private final OrgUnitRepository orgUnitRepository;
    private final OrganizationAccessService organizationAccessService;

    public AddressLineServiceImpl(AddressLineRepository addressLineRepository,
                                  OrgUnitRepository orgUnitRepository,
                                  OrganizationAccessService organizationAccessService) {
        this.addressLineRepository = addressLineRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.organizationAccessService = organizationAccessService;
    }

    @Transactional
    @Override
    public AddressLineRes create(AddressLineCreateReq request) {
        validateRequest(request.orgUnitId(), request.level(), request.parentLine1Id(),
                request.parentLine2Id(), request.parentLine3Id(), request.postalCode(), null);
        if (addressLineRepository.existsByLevelAndNameIgnoreCaseAndParentLine1IdAndParentLine2IdAndParentLine3IdAndOrgUnitId(
                request.level(),
                request.name(),
                request.parentLine1Id(),
                request.parentLine2Id(),
                request.parentLine3Id(),
                request.orgUnitId())) {
            throw new BadRequestException("Address line already exists in the same hierarchy");
        }
        AddressLine line = new AddressLine();
        line.setOrgUnitId(request.orgUnitId());
        line.setLevel(request.level());
        line.setName(request.name().trim());
        line.setParentLine1Id(request.parentLine1Id());
        line.setParentLine2Id(request.parentLine2Id());
        line.setParentLine3Id(request.parentLine3Id());
        line.setPostalCode(request.postalCode());
        line.setInternalCode(generateInternalCode(line));
        return toResponse(addressLineRepository.save(line));
    }

    @Transactional(readOnly = true)
    @Override
    public List<AddressLineRes> search(String query) {
        if (query == null || query.isBlank()) {
            throw new BadRequestException("Search query is required");
        }
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        List<AddressLine> lines = orgUnitId == null
                ? addressLineRepository.findByNameContainingIgnoreCase(query)
                : addressLineRepository.findByOrgUnitIdAndNameContainingIgnoreCase(orgUnitId, query);
        return lines.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public AddressLineRes getById(Long id) {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        AddressLine line = findLine(id, orgUnitId);
        return toResponse(line);
    }

    @Transactional(readOnly = true)
    @Override
    public AddressLineHierarchyRes getHierarchy(Long id) {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        AddressLine line = findLine(id, orgUnitId);
        AddressLine line1 = findLineOrNull(line.getParentLine1Id(), orgUnitId, "Parent line 1 not found");
        AddressLine line2 = findLineOrNull(line.getParentLine2Id(), orgUnitId, "Parent line 2 not found");
        AddressLine line3 = findLineOrNull(line.getParentLine3Id(), orgUnitId, "Parent line 3 not found");
        return new AddressLineHierarchyRes(toResponse(line), toResponseOrNull(line1),
                toResponseOrNull(line2), toResponseOrNull(line3));
    }

    @Transactional(readOnly = true)
    @Override
    public List<AddressLineHierarchyRes> getHierarchies() {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        List<AddressLine> lines = orgUnitId == null
                ? addressLineRepository.findAll()
                : addressLineRepository.findByOrgUnitId(orgUnitId);
        Map<Long, AddressLine> linesById = lines.stream()
                .collect(Collectors.toMap(AddressLine::getId, Function.identity()));
        return lines.stream()
                .sorted(Comparator.comparing(AddressLine::getId))
                .map(line -> {
                    AddressLine line1 = line.getParentLine1Id() == null ? null : linesById.get(line.getParentLine1Id());
                    AddressLine line2 = line.getParentLine2Id() == null ? null : linesById.get(line.getParentLine2Id());
                    AddressLine line3 = line.getParentLine3Id() == null ? null : linesById.get(line.getParentLine3Id());
                    return new AddressLineHierarchyRes(toResponse(line), toResponseOrNull(line1),
                            toResponseOrNull(line2), toResponseOrNull(line3));
                })
                .toList();
    }

    @Transactional
    @Override
    public AddressLineRes update(Long id, AddressLineUpdateReq request) {
        AddressLine line = addressLineRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Address line not found", ErrorCode.NOT_FOUND));
        validateRequest(request.orgUnitId(), request.level(), request.parentLine1Id(),
                request.parentLine2Id(), request.parentLine3Id(), request.postalCode(), id);
        if (addressLineRepository
                .existsByLevelAndNameIgnoreCaseAndParentLine1IdAndParentLine2IdAndParentLine3IdAndOrgUnitIdAndIdNot(
                        request.level(),
                        request.name(),
                        request.parentLine1Id(),
                        request.parentLine2Id(),
                        request.parentLine3Id(),
                        request.orgUnitId(),
                        id)) {
            throw new BadRequestException("Address line already exists in the same hierarchy");
        }
        boolean shouldRegenerateInternalCode = shouldRegenerateInternalCode(line, request);
        line.setOrgUnitId(request.orgUnitId());
        line.setLevel(request.level());
        line.setName(request.name().trim());
        line.setParentLine1Id(request.parentLine1Id());
        line.setParentLine2Id(request.parentLine2Id());
        line.setParentLine3Id(request.parentLine3Id());
        line.setPostalCode(request.postalCode());
        if (shouldRegenerateInternalCode) {
            line.setInternalCode(generateInternalCode(line));
        }
        return toResponse(addressLineRepository.save(line));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        AddressLine line = addressLineRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Address line not found", ErrorCode.NOT_FOUND));
        if (addressLineRepository.existsByParentLine1IdOrParentLine2IdOrParentLine3Id(id, id, id)) {
            throw new BadRequestException("Address line has child entries and cannot be deleted");
        }
        addressLineRepository.delete(line);
    }

    private void validateRequest(Long orgUnitId, Integer level, Long parentLine1Id,
                                 Long parentLine2Id, Long parentLine3Id,
                                 String postalCode, Long currentId) {
        validateOrgUnit(orgUnitId);
        organizationAccessService.enforceOrgUnitAccess(orgUnitId);
        int levelValue = level;
        if (levelValue < 1 || levelValue > 4) {
            throw new BadRequestException("Address line level must be between 1 and 4");
        }
        if (currentId != null) {
            if (currentId.equals(parentLine1Id) || currentId.equals(parentLine2Id) || currentId.equals(parentLine3Id)) {
                throw new BadRequestException("Address line cannot reference itself as a parent");
            }
        }
        if (levelValue == 1) {
            if (parentLine1Id != null || parentLine2Id != null || parentLine3Id != null) {
                throw new BadRequestException("Line 1 cannot have parent references");
            }
            if (postalCode == null || postalCode.isBlank()) {
                throw new BadRequestException("Postal code is required for line 1");
            }
            if (!postalCode.matches("\\d+")) {
                throw new BadRequestException("Postal code must be numeric");
            }
        }
        if (levelValue == 2) {
            requireParent(parentLine1Id, "Line 1");
            if (parentLine2Id != null || parentLine3Id != null) {
                throw new BadRequestException("Line 2 can only link to line 1");
            }
        }
        if (levelValue == 3) {
            requireParent(parentLine1Id, "Line 1");
            requireParent(parentLine2Id, "Line 2");
            if (parentLine3Id != null) {
                throw new BadRequestException("Line 3 can only link to line 1 and 2");
            }
        }
        if (levelValue == 4) {
            requireParent(parentLine1Id, "Line 1");
            requireParent(parentLine2Id, "Line 2");
            requireParent(parentLine3Id, "Line 3");
        }
        if (parentLine1Id != null) {
            validateParentLevel(parentLine1Id, 1);
        }
        if (parentLine2Id != null) {
            validateParentLevel(parentLine2Id, 2);
        }
        if (parentLine3Id != null) {
            validateParentLevel(parentLine3Id, 3);
        }
    }

    private void requireParent(Long parentId, String label) {
        if (parentId == null) {
            throw new BadRequestException(label + " is required for this level");
        }
    }

    private void validateParentLevel(Long parentId, int expectedLevel) {
        AddressLine parent = addressLineRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException("Parent line not found", ErrorCode.NOT_FOUND));
        if (parent.getLevel() != expectedLevel) {
            throw new BadRequestException("Parent line level mismatch");
        }
    }

    private AddressLine findLine(Long id, Long orgUnitId) {
        if (orgUnitId == null) {
            return addressLineRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Address line not found", ErrorCode.NOT_FOUND));
        }
        return addressLineRepository.findByIdAndOrgUnitId(id, orgUnitId)
                .orElseThrow(() -> new NotFoundException("Address line not found", ErrorCode.NOT_FOUND));
    }

    private AddressLine findLineOrNull(Long id, Long orgUnitId, String notFoundMessage) {
        if (id == null) {
            return null;
        }
        if (orgUnitId == null) {
            return addressLineRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException(notFoundMessage, ErrorCode.NOT_FOUND));
        }
        return addressLineRepository.findByIdAndOrgUnitId(id, orgUnitId)
                .orElseThrow(() -> new NotFoundException(notFoundMessage, ErrorCode.NOT_FOUND));
    }

    private void validateOrgUnit(Long orgUnitId) {
        if (orgUnitId == null || !orgUnitRepository.existsById(orgUnitId)) {
            throw new NotFoundException("Org unit not found", ErrorCode.NOT_FOUND);
        }
    }

    private String generateInternalCode(AddressLine line) {
        if (line.getLevel() == 1) {
            return line.getPostalCode();
        }
        String prefix = loadParentInternalCode(line);
        int nextIndex = addressLineRepository.findTopByLevelAndParentLine1IdAndParentLine2IdAndParentLine3IdAndOrgUnitIdOrderByInternalCodeDesc(
                        line.getLevel(),
                        line.getParentLine1Id(),
                        line.getParentLine2Id(),
                        line.getParentLine3Id(),
                        line.getOrgUnitId())
                .map(AddressLine::getInternalCode)
                .map(code -> code.substring(code.lastIndexOf('.') + 1))
                .map(suffix -> {
                    try {
                        return Integer.parseInt(suffix);
                    } catch (NumberFormatException ex) {
                        return 0;
                    }
                })
                .orElse(0);
        return prefix + "." + String.format("%03d", nextIndex + 1);
    }

    private String loadParentInternalCode(AddressLine line) {
        if (line.getLevel() == 2) {
            return addressLineRepository.findById(line.getParentLine1Id())
                    .orElseThrow(() -> new NotFoundException("Parent line 1 not found", ErrorCode.NOT_FOUND))
                    .getInternalCode();
        }
        if (line.getLevel() == 3) {
            return addressLineRepository.findById(line.getParentLine2Id())
                    .orElseThrow(() -> new NotFoundException("Parent line 2 not found", ErrorCode.NOT_FOUND))
                    .getInternalCode();
        }
        return addressLineRepository.findById(line.getParentLine3Id())
                .orElseThrow(() -> new NotFoundException("Parent line 3 not found", ErrorCode.NOT_FOUND))
                .getInternalCode();
    }

    private boolean shouldRegenerateInternalCode(AddressLine line, AddressLineUpdateReq request) {
        if (!line.getLevel().equals(request.level())) {
            return true;
        }
        if (!line.getOrgUnitId().equals(request.orgUnitId())) {
            return true;
        }
        if (!equalsNullable(line.getParentLine1Id(), request.parentLine1Id())
                || !equalsNullable(line.getParentLine2Id(), request.parentLine2Id())
                || !equalsNullable(line.getParentLine3Id(), request.parentLine3Id())) {
            return true;
        }
        if (request.level() == 1) {
            return !equalsNullable(line.getPostalCode(), request.postalCode());
        }
        return false;
    }

    private boolean equalsNullable(Object left, Object right) {
        return left == null ? right == null : left.equals(right);
    }

    private AddressLineRes toResponse(AddressLine line) {
        return new AddressLineRes(
                line.getId(),
                line.getOrgUnitId(),
                line.getLevel(),
                line.getName(),
                line.getParentLine1Id(),
                line.getParentLine2Id(),
                line.getParentLine3Id(),
                line.getPostalCode(),
                line.getCreatedAt(),
                line.getUpdatedAt()
        );
    }

    private AddressLineRes toResponseOrNull(AddressLine line) {
        return line == null ? null : toResponse(line);
    }
}
