package me.nimnakse.water_management.address_lines.service.impl;

import java.util.List;
import me.nimnakse.water_management.address_lines.dto.request.AddressLineCreateReq;
import me.nimnakse.water_management.address_lines.dto.response.AddressLineHierarchyRes;
import me.nimnakse.water_management.address_lines.dto.response.AddressLineRes;
import me.nimnakse.water_management.address_lines.entity.AddressLine;
import me.nimnakse.water_management.address_lines.repository.AddressLineRepository;
import me.nimnakse.water_management.address_lines.service.AddressLineService;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.organization.repository.OrgUnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddressLineServiceImpl implements AddressLineService {
    private final AddressLineRepository addressLineRepository;
    private final OrgUnitRepository orgUnitRepository;

    public AddressLineServiceImpl(AddressLineRepository addressLineRepository,
                                  OrgUnitRepository orgUnitRepository) {
        this.addressLineRepository = addressLineRepository;
        this.orgUnitRepository = orgUnitRepository;
    }

    @Transactional
    @Override
    public AddressLineRes create(AddressLineCreateReq request) {
        validateRequest(request);
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
        return addressLineRepository.findByNameContainingIgnoreCase(query).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public AddressLineHierarchyRes getHierarchy(Long id) {
        AddressLine line = addressLineRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Address line not found", ErrorCode.NOT_FOUND));
        AddressLine line1 = line.getParentLine1Id() == null ? null : addressLineRepository.findById(line.getParentLine1Id())
                .orElseThrow(() -> new NotFoundException("Parent line 1 not found", ErrorCode.NOT_FOUND));
        AddressLine line2 = line.getParentLine2Id() == null ? null : addressLineRepository.findById(line.getParentLine2Id())
                .orElseThrow(() -> new NotFoundException("Parent line 2 not found", ErrorCode.NOT_FOUND));
        AddressLine line3 = line.getParentLine3Id() == null ? null : addressLineRepository.findById(line.getParentLine3Id())
                .orElseThrow(() -> new NotFoundException("Parent line 3 not found", ErrorCode.NOT_FOUND));
        return new AddressLineHierarchyRes(toResponse(line), toResponseOrNull(line1),
                toResponseOrNull(line2), toResponseOrNull(line3));
    }

    private void validateRequest(AddressLineCreateReq request) {
        validateOrgUnit(request.orgUnitId());
        int level = request.level();
        if (level < 1 || level > 4) {
            throw new BadRequestException("Address line level must be between 1 and 4");
        }
        if (level == 1) {
            if (request.parentLine1Id() != null || request.parentLine2Id() != null || request.parentLine3Id() != null) {
                throw new BadRequestException("Line 1 cannot have parent references");
            }
            if (request.postalCode() == null || request.postalCode().isBlank()) {
                throw new BadRequestException("Postal code is required for line 1");
            }
            if (!request.postalCode().matches("\\d+")) {
                throw new BadRequestException("Postal code must be numeric");
            }
        }
        if (level == 2) {
            requireParent(request.parentLine1Id(), "Line 1");
            if (request.parentLine2Id() != null || request.parentLine3Id() != null) {
                throw new BadRequestException("Line 2 can only link to line 1");
            }
        }
        if (level == 3) {
            requireParent(request.parentLine1Id(), "Line 1");
            requireParent(request.parentLine2Id(), "Line 2");
            if (request.parentLine3Id() != null) {
                throw new BadRequestException("Line 3 can only link to line 1 and 2");
            }
        }
        if (level == 4) {
            requireParent(request.parentLine1Id(), "Line 1");
            requireParent(request.parentLine2Id(), "Line 2");
            requireParent(request.parentLine3Id(), "Line 3");
        }
        if (request.parentLine1Id() != null) {
            validateParentLevel(request.parentLine1Id(), 1);
        }
        if (request.parentLine2Id() != null) {
            validateParentLevel(request.parentLine2Id(), 2);
        }
        if (request.parentLine3Id() != null) {
            validateParentLevel(request.parentLine3Id(), 3);
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
