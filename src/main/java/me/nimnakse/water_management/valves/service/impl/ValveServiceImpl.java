package me.nimnakse.water_management.valves.service.impl;

import java.util.List;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.connections.repository.ConnectionRepository;
import me.nimnakse.water_management.security.OrganizationAccessService;
import me.nimnakse.water_management.valves.dto.request.ValveCreateReq;
import me.nimnakse.water_management.valves.dto.request.ValveUpdateReq;
import me.nimnakse.water_management.valves.dto.response.ValveRes;
import me.nimnakse.water_management.valves.entity.Valve;
import me.nimnakse.water_management.valves.repository.ValveRepository;
import me.nimnakse.water_management.valves.service.ValveService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ValveServiceImpl implements ValveService {
    private final ValveRepository valveRepository;
    private final ConnectionRepository connectionRepository;
    private final OrganizationAccessService accessService;

    public ValveServiceImpl(ValveRepository valveRepository,
            ConnectionRepository connectionRepository,
            OrganizationAccessService accessService) {
        this.valveRepository = valveRepository;
        this.connectionRepository = connectionRepository;
        this.accessService = accessService;
    }

    @Transactional
    @Override
    public ValveRes create(ValveCreateReq request) {
        Long orgUnitId = request.orgUnitId() != null ? request.orgUnitId() : accessService.resolveOrgUnitId();
        accessService.enforceOrgUnitAccess(orgUnitId);

        if (valveRepository.existsByOrgUnitIdAndNameIgnoreCase(orgUnitId, request.name())) {
            throw new BadRequestException("Valve already exists", "කපාටය දැනටමත් පවතී");
        }
        Valve valve = new Valve();
        valve.setOrgUnitId(orgUnitId);
        valve.setClusterId(request.clusterId());
        valve.setName(request.name().trim());
        return toResponse(valveRepository.save(valve));
    }

    @Transactional
    @Override
    public ValveRes update(Long id, ValveUpdateReq request) {
        Valve valve = valveRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Valve not found", "කපාටය සොයාගත නොහැක", ErrorCode.NOT_FOUND));

        Long orgUnitId = request.orgUnitId() != null ? request.orgUnitId() : valve.getOrgUnitId();
        accessService.enforceOrgUnitAccess(orgUnitId);

        if (valveRepository.existsByOrgUnitIdAndNameIgnoreCaseAndIdNot(orgUnitId, request.name(), id)) {
            throw new BadRequestException("Valve already exists", "කපාටය දැනටමත් පවතී");
        }
        valve.setOrgUnitId(orgUnitId);
        valve.setClusterId(request.clusterId());
        valve.setName(request.name().trim());
        return toResponse(valveRepository.save(valve));
    }

    @Transactional(readOnly = true)
    @Override
    public ValveRes getById(Long id) {
        Valve valve = valveRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Valve not found", "කපාටය සොයාගත නොහැක", ErrorCode.NOT_FOUND));
        accessService.enforceOrgUnitAccess(valve.getOrgUnitId());
        return toResponse(valve);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ValveRes> list(Long orgUnitId) {
        Long resolvedOrgUnitId = orgUnitId != null ? orgUnitId : accessService.resolveOrgUnitId();
        if (resolvedOrgUnitId != null) {
            accessService.enforceOrgUnitAccess(resolvedOrgUnitId);
            return valveRepository.findByOrgUnitId(resolvedOrgUnitId).stream()
                    .map(this::toResponse)
                    .toList();
        }
        return valveRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Valve valve = valveRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Valve not found", "කපාටය සොයාගත නොහැක", ErrorCode.NOT_FOUND));
        accessService.enforceOrgUnitAccess(valve.getOrgUnitId());
        if (connectionRepository.existsByValveId(id)) {
            throw new BadRequestException("Valve is linked to connections and cannot be deleted",
                    "කපාටය සම්බන්ධතාවලට සම්බන්ධ කර ඇති බැවින් මකා දැමිය නොහැක");
        }
        valveRepository.delete(valve);
    }

    private ValveRes toResponse(Valve valve) {
        return new ValveRes(
                valve.getId(),
                valve.getName(),
                valve.getOrgUnitId(),
                valve.getClusterId(),
                valve.getCreatedAt(),
                valve.getUpdatedAt());
    }
}
