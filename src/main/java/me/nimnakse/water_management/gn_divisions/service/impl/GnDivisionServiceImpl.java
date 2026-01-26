package me.nimnakse.water_management.gn_divisions.service.impl;

import java.util.List;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.connections.repository.ConnectionRepository;
import me.nimnakse.water_management.gn_divisions.dto.request.GnDivisionCreateReq;
import me.nimnakse.water_management.gn_divisions.dto.request.GnDivisionUpdateReq;
import me.nimnakse.water_management.gn_divisions.dto.response.GnDivisionRes;
import me.nimnakse.water_management.gn_divisions.entity.GnDivision;
import me.nimnakse.water_management.gn_divisions.repository.GnDivisionRepository;
import me.nimnakse.water_management.gn_divisions.service.GnDivisionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GnDivisionServiceImpl implements GnDivisionService {
    private final GnDivisionRepository gnDivisionRepository;
    private final ConnectionRepository connectionRepository;
    private final me.nimnakse.water_management.security.OrganizationAccessService accessService;

    public GnDivisionServiceImpl(GnDivisionRepository gnDivisionRepository,
            ConnectionRepository connectionRepository,
            me.nimnakse.water_management.security.OrganizationAccessService accessService) {
        this.gnDivisionRepository = gnDivisionRepository;
        this.connectionRepository = connectionRepository;
        this.accessService = accessService;
    }

    @Transactional
    @Override
    public GnDivisionRes create(GnDivisionCreateReq request) {
        Long orgUnitId = request.orgUnitId() != null ? request.orgUnitId() : accessService.resolveOrgUnitId();
        accessService.enforceOrgUnitAccess(orgUnitId);

        if (gnDivisionRepository.existsByOrgUnitIdAndNameIgnoreCase(orgUnitId, request.name())) {
            throw new BadRequestException("GN division already exists", "ග්‍රාම නිලධාරී වසම දැනටමත් පවතී");
        }
        GnDivision division = new GnDivision();
        division.setOrgUnitId(orgUnitId);
        division.setClusterId(request.clusterId());
        division.setName(request.name().trim());
        return toResponse(gnDivisionRepository.save(division));
    }

    @Transactional
    @Override
    public GnDivisionRes update(Long id, GnDivisionUpdateReq request) {
        GnDivision division = gnDivisionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("GN division not found", "ග්‍රාම නිලධාරී වසම සොයාගත නොහැක",
                        ErrorCode.NOT_FOUND));

        Long orgUnitId = request.orgUnitId() != null ? request.orgUnitId() : division.getOrgUnitId();
        accessService.enforceOrgUnitAccess(orgUnitId);

        if (gnDivisionRepository.existsByOrgUnitIdAndNameIgnoreCaseAndIdNot(orgUnitId, request.name(), id)) {
            throw new BadRequestException("GN division already exists", "ග්‍රාම නිලධාරී වසම දැනටමත් පවතී");
        }
        division.setOrgUnitId(orgUnitId);
        division.setClusterId(request.clusterId());
        division.setName(request.name().trim());
        return toResponse(gnDivisionRepository.save(division));
    }

    @Transactional(readOnly = true)
    @Override
    public GnDivisionRes getById(Long id) {
        GnDivision division = gnDivisionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("GN division not found", "ග්‍රාම නිලධාරී වසම සොයාගත නොහැක",
                        ErrorCode.NOT_FOUND));
        accessService.enforceOrgUnitAccess(division.getOrgUnitId());
        return toResponse(division);
    }

    @Transactional(readOnly = true)
    @Override
    public List<GnDivisionRes> list(Long orgUnitId) {
        Long resolvedOrgUnitId = orgUnitId != null ? orgUnitId : accessService.resolveOrgUnitId();
        if (resolvedOrgUnitId != null) {
            accessService.enforceOrgUnitAccess(resolvedOrgUnitId);
            return gnDivisionRepository.findByOrgUnitId(resolvedOrgUnitId).stream()
                    .map(this::toResponse)
                    .toList();
        }
        return gnDivisionRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long id) {
        GnDivision division = gnDivisionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("GN division not found", "ග්‍රාම නිලධාරී වසම සොයාගත නොහැක",
                        ErrorCode.NOT_FOUND));
        accessService.enforceOrgUnitAccess(division.getOrgUnitId());
        if (connectionRepository.existsByGnDivisionId(id)) {
            throw new BadRequestException("GN division is linked to connections and cannot be deleted",
                    "ග්‍රාම නිලධාරී වසම සම්බන්ධතාවලට සම්බන්ධ කර ඇති බැවින් මකා දැමිය නොහැක");
        }
        gnDivisionRepository.delete(division);
    }

    private GnDivisionRes toResponse(GnDivision division) {
        return new GnDivisionRes(
                division.getId(),
                division.getName(),
                division.getOrgUnitId(),
                division.getClusterId(),
                division.getCreatedAt(),
                division.getUpdatedAt());
    }
}
