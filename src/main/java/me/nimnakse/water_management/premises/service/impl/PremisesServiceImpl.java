package me.nimnakse.water_management.premises.service.impl;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import me.nimnakse.water_management.billing_zones.entity.BillingZone;
import me.nimnakse.water_management.billing_zones.repository.BillingZoneRepository;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.connections.repository.ConnectionRepository;
import me.nimnakse.water_management.premises.dto.request.PremisesCreateReq;
import me.nimnakse.water_management.premises.dto.request.PremisesUpdateReq;
import me.nimnakse.water_management.premises.dto.response.PremisesNextAvailableRes;
import me.nimnakse.water_management.premises.dto.response.PremisesRes;
import me.nimnakse.water_management.premises.entity.Premises;
import me.nimnakse.water_management.premises.repository.PremisesRepository;
import me.nimnakse.water_management.premises.service.PremisesService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PremisesServiceImpl implements PremisesService {
    private final PremisesRepository premisesRepository;
    private final BillingZoneRepository billingZoneRepository;
    private final ConnectionRepository connectionRepository;

    public PremisesServiceImpl(PremisesRepository premisesRepository,
            BillingZoneRepository billingZoneRepository,
            ConnectionRepository connectionRepository) {
        this.premisesRepository = premisesRepository;
        this.billingZoneRepository = billingZoneRepository;
        this.connectionRepository = connectionRepository;
    }

    @Transactional
    @Override
    public PremisesRes create(PremisesCreateReq request) {
        validateBillingZone(request.billingZoneId());
        validateParent(request.billingZoneId(), request.parentId(), null);
        if (premisesRepository.existsByBillingZoneIdAndPremisesCode(request.billingZoneId(), request.premisesCode())) {
            throw new BadRequestException("Premises code already exists in the billing zone",
                    "බිල්පත් කලාපය තුළ පරිශ්‍ර කේතය දැනටමත් පවතී");
        }
        Premises premises = new Premises();
        premises.setBillingZoneId(request.billingZoneId());
        premises.setPremisesCode(request.premisesCode().trim());
        premises.setSortPath(request.sortPath().trim());
        premises.setParentId(request.parentId());
        return toResponse(premisesRepository.save(premises));
    }

    @Transactional
    @Override
    public PremisesRes update(Long id, PremisesUpdateReq request) {
        Premises premises = premisesRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Premises not found", "පරිශ්‍රය සොයාගත නොහැක",
                        ErrorCode.NOT_FOUND));
        validateBillingZone(request.billingZoneId());
        validateParent(request.billingZoneId(), request.parentId(), id);
        if (premisesRepository.existsByBillingZoneIdAndPremisesCodeAndIdNot(
                request.billingZoneId(), request.premisesCode(), id)) {
            throw new BadRequestException("Premises code already exists in the billing zone",
                    "බිල්පත් කලාපය තුළ පරිශ්‍ර කේතය දැනටමත් පවතී");
        }
        premises.setBillingZoneId(request.billingZoneId());
        premises.setPremisesCode(request.premisesCode().trim());
        premises.setSortPath(request.sortPath().trim());
        premises.setParentId(request.parentId());
        return toResponse(premisesRepository.save(premises));
    }

    @Transactional(readOnly = true)
    @Override
    public PremisesRes getById(Long id) {
        Premises premises = premisesRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Premises not found", "පරිශ්‍රය සොයාගත නොහැක",
                        ErrorCode.NOT_FOUND));
        return toResponse(premises);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PremisesRes> list(Long billingZoneId) {
        List<Premises> premises = billingZoneId == null
                ? premisesRepository.findAll()
                : premisesRepository.findByBillingZoneIdOrderBySortPathAsc(billingZoneId);
        return premises.stream()
                .sorted(Comparator.comparing(Premises::getBillingZoneId)
                        .thenComparing(Premises::getSortPath))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public PremisesNextAvailableRes nextAvailable(Long billingZoneId, Long parentId) {
        BillingZone zone = loadBillingZone(billingZoneId);
        Premises parent = loadParentForNextAvailable(billingZoneId, parentId);
        List<Premises> siblings = parentId == null
                ? premisesRepository.findByBillingZoneIdAndParentIdIsNull(billingZoneId)
                : premisesRepository.findByBillingZoneIdAndParentId(billingZoneId, parentId);

        NextSegment nextSegment = resolveNextSegment(siblings, parentId == null ? 3 : 4);
        String sortPath = parent == null
                ? nextSegment.segment()
                : parent.getSortPath() + "." + nextSegment.segment();
        String premisesCode = buildPremisesCode(zone.getZoneCode(), parent, nextSegment.index());

        return new PremisesNextAvailableRes(billingZoneId, parentId, premisesCode, sortPath);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Premises premises = premisesRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Premises not found", "පරිශ්‍රය සොයාගත නොහැක",
                        ErrorCode.NOT_FOUND));
        if (connectionRepository.existsByPremisesId(id)) {
            throw new BadRequestException("Premises is linked to a connection and cannot be deleted",
                    "පරිශ්‍රය සම්බන්ධතාවයකට සම්බන්ධ කර ඇති බැවින් මකා දැමිය නොහැක");
        }
        if (premisesRepository.existsByParentId(id)) {
            throw new BadRequestException("Premises has child entries and cannot be deleted",
                    "අනු ඇතුළත් කිරීම් පවතින පරිශ්‍ර මකා දැමිය නොහැක");
        }
        premisesRepository.delete(premises);
    }

    private void validateBillingZone(Long billingZoneId) {
        if (billingZoneId == null || !billingZoneRepository.existsById(billingZoneId)) {
            throw new NotFoundException("Billing zone not found", "බිල්පත් කලාපය සොයාගත නොහැක", ErrorCode.NOT_FOUND);
        }
    }

    private BillingZone loadBillingZone(Long billingZoneId) {
        if (billingZoneId == null) {
            throw new NotFoundException("Billing zone not found", "බිල්පත් කලාපය සොයාගත නොහැක", ErrorCode.NOT_FOUND);
        }
        return billingZoneRepository.findById(billingZoneId)
                .orElseThrow(() -> new NotFoundException("Billing zone not found", "බිල්පත් කලාපය සොයාගත නොහැක",
                        ErrorCode.NOT_FOUND));
    }

    private Premises loadParentForNextAvailable(Long billingZoneId, Long parentId) {
        if (parentId == null) {
            return null;
        }
        Premises parent = premisesRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException("Parent premises not found", "දෙමව් පරිශ්‍රය සොයාගත නොහැක",
                        ErrorCode.NOT_FOUND));
        if (!parent.getBillingZoneId().equals(billingZoneId)) {
            throw new BadRequestException("Parent premises must belong to the same billing zone",
                    "දෙමව් පරිශ්‍රය එකම බිල්පත් කලාපයට අයත් විය යුතුය");
        }
        return parent;
    }

    private void validateParent(Long billingZoneId, Long parentId, Long currentId) {
        if (parentId == null) {
            return;
        }
        if (currentId != null && currentId.equals(parentId)) {
            throw new BadRequestException("Premises cannot reference itself as a parent",
                    "පරිශ්‍රය එහිම දෙමාපිය පරිශ්‍රයක් ලෙස යොමු කළ නොහැක");
        }
        Premises parent = premisesRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException("Parent premises not found", "දෙමාපිය පරිශ්‍රය සොයාගත නොහැක",
                        ErrorCode.NOT_FOUND));
        if (!parent.getBillingZoneId().equals(billingZoneId)) {
            throw new BadRequestException("Parent premises must belong to the same billing zone",
                    "දෙමාපිය පරිශ්‍රය එකම බිල්පත් කලාපයට අයත් විය යුතුය");
        }
    }

    private PremisesRes toResponse(Premises premises) {
        return new PremisesRes(
                premises.getId(),
                premises.getBillingZoneId(),
                premises.getPremisesCode(),
                premises.getSortPath(),
                premises.getParentId(),
                premises.getCreatedAt(),
                premises.getUpdatedAt());
    }

    private String buildPremisesCode(String zoneCode, Premises parent, int nextIndex) {
        if (parent == null) {
            return zoneCode + nextIndex;
        }
        return parent.getPremisesCode() + "." + nextIndex;
    }

    private NextSegment resolveNextSegment(List<Premises> siblings, int defaultWidth) {
        Set<Integer> used = new HashSet<>();
        int maxWidth = 0;
        for (Premises sibling : siblings) {
            String sortPath = sibling.getSortPath();
            if (sortPath == null || sortPath.isBlank()) {
                continue;
            }
            String segment = lastSegment(sortPath);
            if (segment.isEmpty()) {
                continue;
            }
            maxWidth = Math.max(maxWidth, segment.length());
            try {
                int value = Integer.parseInt(segment);
                if (value > 0) {
                    used.add(value);
                }
            } catch (NumberFormatException ignored) {
                // Ignore non-numeric segments when calculating next index.
            }
        }
        int nextIndex = 1;
        while (used.contains(nextIndex)) {
            nextIndex++;
        }
        int width = maxWidth > 0 ? maxWidth : defaultWidth;
        String segment = String.format("%0" + width + "d", nextIndex);
        return new NextSegment(nextIndex, segment);
    }

    private String lastSegment(String sortPath) {
        int lastDot = sortPath.lastIndexOf('.');
        if (lastDot < 0) {
            return sortPath.trim();
        }
        return sortPath.substring(lastDot + 1).trim();
    }

    private record NextSegment(int index, String segment) {
    }
}
