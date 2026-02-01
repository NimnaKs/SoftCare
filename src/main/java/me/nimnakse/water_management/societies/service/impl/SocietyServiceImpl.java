package me.nimnakse.water_management.societies.service.impl;

import java.util.List;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.connections.repository.ConnectionRepository;
import me.nimnakse.water_management.security.OrganizationAccessService;
import me.nimnakse.water_management.societies.dto.request.SocietyCreateReq;
import me.nimnakse.water_management.societies.dto.request.SocietyUpdateReq;
import me.nimnakse.water_management.societies.dto.response.SocietyRes;
import me.nimnakse.water_management.societies.entity.Society;
import me.nimnakse.water_management.societies.repository.SocietyRepository;
import me.nimnakse.water_management.societies.service.SocietyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SocietyServiceImpl implements SocietyService {
    private final SocietyRepository societyRepository;
    private final ConnectionRepository connectionRepository;
    private final OrganizationAccessService accessService;

    public SocietyServiceImpl(SocietyRepository societyRepository,
            ConnectionRepository connectionRepository,
            OrganizationAccessService accessService) {
        this.societyRepository = societyRepository;
        this.connectionRepository = connectionRepository;
        this.accessService = accessService;
    }

    @Transactional
    @Override
    public SocietyRes create(SocietyCreateReq request) {
        Long orgUnitId = request.orgUnitId() != null ? request.orgUnitId() : accessService.resolveOrgUnitId();
        accessService.enforceOrgUnitAccess(orgUnitId);

        if (societyRepository.existsByOrgUnitIdAndNameIgnoreCase(orgUnitId, request.name())) {
            throw new BadRequestException("Society already exists", "සමිතිය දැනටමත් පවතී");
        }
        Society society = new Society();
        society.setOrgUnitId(orgUnitId);
        society.setName(request.name().trim());
        return toResponse(societyRepository.save(society));
    }

    @Transactional
    @Override
    public SocietyRes update(Long id, SocietyUpdateReq request) {
        Society society = societyRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException("Society not found", "සමිතිය සොයාගත නොහැක", ErrorCode.NOT_FOUND));

        Long orgUnitId = request.orgUnitId() != null ? request.orgUnitId() : society.getOrgUnitId();
        accessService.enforceOrgUnitAccess(orgUnitId);

        if (societyRepository.existsByOrgUnitIdAndNameIgnoreCaseAndIdNot(orgUnitId, request.name(), id)) {
            throw new BadRequestException("Society already exists", "සමිතිය දැනටමත් පවතී");
        }
        society.setOrgUnitId(orgUnitId);
        society.setName(request.name().trim());
        return toResponse(societyRepository.save(society));
    }

    @Transactional(readOnly = true)
    @Override
    public SocietyRes getById(Long id) {
        Society society = societyRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException("Society not found", "සමිතිය සොයාගත නොහැක", ErrorCode.NOT_FOUND));
        accessService.enforceOrgUnitAccess(society.getOrgUnitId());
        return toResponse(society);
    }

    @Transactional(readOnly = true)
    @Override
    public List<SocietyRes> list(Long orgUnitId) {
        Long resolvedOrgUnitId = orgUnitId != null ? orgUnitId : accessService.resolveOrgUnitId();
        if (resolvedOrgUnitId != null) {
            accessService.enforceOrgUnitAccess(resolvedOrgUnitId);
            return societyRepository.findByOrgUnitId(resolvedOrgUnitId).stream()
                    .map(this::toResponse)
                    .toList();
        }
        return societyRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Society society = societyRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException("Society not found", "සමිතිය සොයාගත නොහැක", ErrorCode.NOT_FOUND));
        accessService.enforceOrgUnitAccess(society.getOrgUnitId());
        if (connectionRepository.existsBySocietyId(id)) {
            throw new BadRequestException("Society is linked to connections and cannot be deleted",
                    "සමිතිය සම්බන්ධතාවලට සම්බන්ධ කර ඇති බැවින් මකා දැමිය නොහැක");
        }
        societyRepository.delete(society);
    }

    private SocietyRes toResponse(Society society) {
        return new SocietyRes(
                society.getId(),
                society.getName(),
                society.getOrgUnitId(),
                society.getCreatedAt(),
                society.getUpdatedAt());
    }
}
