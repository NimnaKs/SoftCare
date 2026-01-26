package me.nimnakse.water_management.tariffs.service.impl;

import java.util.List;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.connections.repository.ConnectionRepository;
import me.nimnakse.water_management.security.OrganizationAccessService;
import me.nimnakse.water_management.tariffs.dto.request.TariffCreateReq;
import me.nimnakse.water_management.tariffs.dto.request.TariffUpdateReq;
import me.nimnakse.water_management.tariffs.dto.response.TariffRes;
import me.nimnakse.water_management.tariffs.entity.Tariff;
import me.nimnakse.water_management.tariffs.repository.TariffRepository;
import me.nimnakse.water_management.tariffs.service.TariffService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TariffServiceImpl implements TariffService {
    private final TariffRepository tariffRepository;
    private final ConnectionRepository connectionRepository;
    private final OrganizationAccessService accessService;

    public TariffServiceImpl(TariffRepository tariffRepository,
            ConnectionRepository connectionRepository,
            OrganizationAccessService accessService) {
        this.tariffRepository = tariffRepository;
        this.connectionRepository = connectionRepository;
        this.accessService = accessService;
    }

    @Transactional
    @Override
    public TariffRes create(TariffCreateReq request) {
        Long orgUnitId = request.orgUnitId() != null ? request.orgUnitId() : accessService.resolveOrgUnitId();
        accessService.enforceOrgUnitAccess(orgUnitId);

        if (tariffRepository.existsByOrgUnitIdAndNameIgnoreCase(orgUnitId, request.name())) {
            throw new BadRequestException("Tariff already exists", "ගාස්තු ක්‍රමය දැනටමත් පවතී");
        }
        Tariff tariff = new Tariff();
        tariff.setOrgUnitId(orgUnitId);
        tariff.setName(request.name().trim());
        tariff.setDescription(request.description() != null ? request.description().trim() : null);
        return toResponse(tariffRepository.save(tariff));
    }

    @Transactional
    @Override
    public TariffRes update(Long id, TariffUpdateReq request) {
        Tariff tariff = tariffRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException("Tariff not found", "ගාස්තු ක්‍රමය සොයාගත නොහැක",
                                ErrorCode.NOT_FOUND));

        Long orgUnitId = request.orgUnitId() != null ? request.orgUnitId() : tariff.getOrgUnitId();
        accessService.enforceOrgUnitAccess(orgUnitId);

        if (tariffRepository.existsByOrgUnitIdAndNameIgnoreCaseAndIdNot(orgUnitId, request.name(), id)) {
            throw new BadRequestException("Tariff already exists", "ගාස්තු ක්‍රමය දැනටමත් පවතී");
        }
        tariff.setOrgUnitId(orgUnitId);
        tariff.setName(request.name().trim());
        tariff.setDescription(request.description() != null ? request.description().trim() : null);
        return toResponse(tariffRepository.save(tariff));
    }

    @Transactional(readOnly = true)
    @Override
    public TariffRes getById(Long id) {
        Tariff tariff = tariffRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException("Tariff not found", "ගාස්තු ක්‍රමය සොයාගත නොහැක",
                                ErrorCode.NOT_FOUND));
        accessService.enforceOrgUnitAccess(tariff.getOrgUnitId());
        return toResponse(tariff);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TariffRes> list(Long orgUnitId) {
        Long resolvedOrgUnitId = orgUnitId != null ? orgUnitId : accessService.resolveOrgUnitId();
        if (resolvedOrgUnitId != null) {
            accessService.enforceOrgUnitAccess(resolvedOrgUnitId);
            return tariffRepository.findByOrgUnitId(resolvedOrgUnitId).stream()
                    .map(this::toResponse)
                    .toList();
        }
        return tariffRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Tariff tariff = tariffRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException("Tariff not found", "ගාස්තු ක්‍රමය සොයාගත නොහැක",
                                ErrorCode.NOT_FOUND));
        accessService.enforceOrgUnitAccess(tariff.getOrgUnitId());
        if (connectionRepository.existsByTariffId(id)) {
            throw new BadRequestException("Tariff is linked to connections and cannot be deleted",
                    "ගාස්තු ක්‍රමය සම්බන්ධතාවලට සම්බන්ධ කර ඇති බැවින් මකා දැමිය නොහැක");
        }
        tariffRepository.delete(tariff);
    }

    private TariffRes toResponse(Tariff tariff) {
        return new TariffRes(
                tariff.getId(),
                tariff.getName(),
                tariff.getDescription(),
                tariff.getOrgUnitId(),
                tariff.getCreatedAt(),
                tariff.getUpdatedAt());
    }
}
