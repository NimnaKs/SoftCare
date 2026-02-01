package me.nimnakse.water_management.billing_zones.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import me.nimnakse.water_management.billing_zones.dto.request.BillingZoneCreateReq;
import me.nimnakse.water_management.billing_zones.dto.request.BillingZoneReorderReq;
import me.nimnakse.water_management.billing_zones.dto.request.BillingZoneUpdateReq;
import me.nimnakse.water_management.billing_zones.dto.response.BillingZoneRes;
import me.nimnakse.water_management.billing_zones.entity.BillingZone;
import me.nimnakse.water_management.billing_zones.repository.BillingZoneRepository;
import me.nimnakse.water_management.billing_zones.service.BillingZoneService;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.connections.repository.ConnectionRepository;
import me.nimnakse.water_management.security.OrganizationAccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillingZoneServiceImpl implements BillingZoneService {
    private final BillingZoneRepository billingZoneRepository;
    private final OrganizationAccessService accessService;
    private final ConnectionRepository connectionRepository;

    public BillingZoneServiceImpl(BillingZoneRepository billingZoneRepository,
            OrganizationAccessService accessService,
            ConnectionRepository connectionRepository) {
        this.billingZoneRepository = billingZoneRepository;
        this.accessService = accessService;
        this.connectionRepository = connectionRepository;
    }

    @Transactional
    @Override
    public BillingZoneRes create(BillingZoneCreateReq request) {
        Long orgUnitId = request.orgUnitId() != null ? request.orgUnitId() : accessService.resolveOrgUnitId();
        accessService.enforceOrgUnitAccess(orgUnitId);

        if (billingZoneRepository.existsByOrgUnitIdAndZoneNameIgnoreCase(orgUnitId, request.zoneName())) {
            throw new BadRequestException("Billing zone already exists in the org unit",
                    "සංවිධාන ඒකකයේ බිල්පත් කලාපය දැනටමත් පවතී");
        }
        BillingZone zone = new BillingZone();
        zone.setOrgUnitId(orgUnitId);
        zone.setZoneName(request.zoneName().trim());
        zone.setDescription(trimToNull(request.description()));
        zone.setZoneCode(generateZoneCode(orgUnitId));
        zone.setSequenceNumber(resolveSequenceNumber(orgUnitId, request.sequenceNumber()));
        return toResponse(billingZoneRepository.save(zone));
    }

    @Transactional
    @Override
    public BillingZoneRes update(Long id, BillingZoneUpdateReq request) {
        BillingZone zone = billingZoneRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Billing zone not found", "බිල්පත් කලාපය සොයාගත නොහැක",
                        ErrorCode.NOT_FOUND));

        Long orgUnitId = request.orgUnitId() != null ? request.orgUnitId() : zone.getOrgUnitId();
        accessService.enforceOrgUnitAccess(orgUnitId);

        if (billingZoneRepository.existsByOrgUnitIdAndZoneNameIgnoreCaseAndIdNot(orgUnitId, request.zoneName(), id)) {
            throw new BadRequestException("Billing zone already exists in the org unit",
                    "සංවිධාන ඒකකයේ බිල්පත් කලාපය දැනටමත් පවතී");
        }
        zone.setOrgUnitId(orgUnitId);
        zone.setZoneName(request.zoneName().trim());
        zone.setDescription(trimToNull(request.description()));
        zone.setSequenceNumber(validateSequenceNumber(request.sequenceNumber()));
        return toResponse(billingZoneRepository.save(zone));
    }

    @Transactional(readOnly = true)
    @Override
    public BillingZoneRes getById(Long id) {
        BillingZone zone = billingZoneRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Billing zone not found", "බිල්පත් කලාපය සොයාගත නොහැක",
                        ErrorCode.NOT_FOUND));
        accessService.enforceOrgUnitAccess(zone.getOrgUnitId());
        return toResponse(zone);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BillingZoneRes> list(Long orgUnitId) {
        Long resolvedOrgUnitId = orgUnitId != null ? orgUnitId : accessService.resolveOrgUnitId();
        List<BillingZone> zones;
        if (resolvedOrgUnitId != null) {
            accessService.enforceOrgUnitAccess(resolvedOrgUnitId);
            zones = billingZoneRepository.findByOrgUnitIdOrderBySequenceNumberAsc(resolvedOrgUnitId);
        } else {
            zones = billingZoneRepository.findAll();
        }
        if (resolvedOrgUnitId != null) {
            return zones.stream().map(this::toResponse).toList();
        }
        return zones.stream()
                .sorted(Comparator.comparing(BillingZone::getOrgUnitId)
                        .thenComparing(BillingZone::getSequenceNumber))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long id) {
        BillingZone zone = billingZoneRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Billing zone not found", "බිල්පත් කලාපය සොයාගත නොහැක",
                        ErrorCode.NOT_FOUND));
        accessService.enforceOrgUnitAccess(zone.getOrgUnitId());
        if (connectionRepository.existsByBillingZoneId(id)) {
            throw new BadRequestException("Billing zone is linked to connections and cannot be deleted",
                    "බිල්පත් කලාපය සම්බන්ධතා සමඟ සම්බන්ධ වී ඇති අතර මකා දැමිය නොහැක");
        }
        billingZoneRepository.delete(zone);
    }

    @Transactional
    @Override
    public List<BillingZoneRes> reorder(BillingZoneReorderReq request) {
        if (request.zoneIdOrder() == null || request.zoneIdOrder().isEmpty()) {
            throw new BadRequestException("Order list cannot be empty", "පිළිවෙල ලැයිස්තුව හිස් විය නොහැක");
        }

        List<BillingZone> zones = billingZoneRepository.findAllById(request.zoneIdOrder());
        if (zones.size() != request.zoneIdOrder().size()) {
            throw new NotFoundException("Some billing zones were not found", "සමහර බිල්පත් කලාප සොයාගත නොහැක",
                    ErrorCode.NOT_FOUND);
        }

        // Validate all belong to the same org unit (optional but recommended)
        Long orgUnitId = zones.get(0).getOrgUnitId();
        accessService.enforceOrgUnitAccess(orgUnitId);
        for (BillingZone z : zones) {
            if (!z.getOrgUnitId().equals(orgUnitId)) {
                throw new BadRequestException("All zones must belong to the same organization unit",
                        "සියලුම කලාප එකම සංවිධාන ඒකකයකට අයත් විය යුතුය");
            }
        }

        // Map for quick lookup
        java.util.Map<Long, BillingZone> zoneMap = zones.stream()
                .collect(Collectors.toMap(BillingZone::getId, z -> z));

        // Update sequence numbers based on the provided order
        for (int i = 0; i < request.zoneIdOrder().size(); i++) {
            Long id = request.zoneIdOrder().get(i);
            BillingZone zone = zoneMap.get(id);
            zone.setSequenceNumber(i + 1);
        }

        billingZoneRepository.saveAll(zones);

        return zones.stream()
                .sorted(Comparator.comparing(BillingZone::getSequenceNumber))
                .map(this::toResponse)
                .toList();
    }

    private int resolveSequenceNumber(Long orgUnitId, Integer requestSequence) {
        if (requestSequence != null) {
            return validateSequenceNumber(requestSequence);
        }
        int next = billingZoneRepository.findTopByOrgUnitIdOrderBySequenceNumberDesc(orgUnitId)
                .map(BillingZone::getSequenceNumber)
                .orElse(0);
        return next + 1;
    }

    private int validateSequenceNumber(Integer sequenceNumber) {
        if (sequenceNumber == null || sequenceNumber < 1) {
            throw new BadRequestException("Sequence number must be at least 1",
                    "අනුක්‍රමික අංකය අවම වශයෙන් 1 විය යුතුය");
        }
        return sequenceNumber;
    }

    private String generateZoneCode(Long orgUnitId) {
        Set<String> used = billingZoneRepository.findByOrgUnitId(orgUnitId).stream()
                .map(BillingZone::getZoneCode)
                .collect(Collectors.toSet());

        int index = 0;
        while (true) {
            String candidate = convertToLetter(index++);
            if (!used.contains(candidate)) {
                return candidate;
            }
        }
    }

    private String convertToLetter(int index) {
        StringBuilder sb = new StringBuilder();
        int n = index;
        while (n >= 0) {
            sb.insert(0, (char) ('A' + (n % 26)));
            n = (n / 26) - 1;
        }
        return sb.toString();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private BillingZoneRes toResponse(BillingZone zone) {
        return new BillingZoneRes(
                zone.getId(),
                zone.getOrgUnitId(),
                zone.getZoneName(),
                zone.getDescription(),
                zone.getZoneCode(),
                zone.getSequenceNumber(),
                zone.getCreatedAt(),
                zone.getUpdatedAt());
    }
}
