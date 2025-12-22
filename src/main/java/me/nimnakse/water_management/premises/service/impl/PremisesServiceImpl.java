package me.nimnakse.water_management.premises.service.impl;

import java.util.Comparator;
import java.util.List;
import me.nimnakse.water_management.billing_zones.repository.BillingZoneRepository;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.connections.repository.ConnectionRepository;
import me.nimnakse.water_management.premises.dto.request.PremisesCreateReq;
import me.nimnakse.water_management.premises.dto.request.PremisesUpdateReq;
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
            throw new BadRequestException("Premises code already exists in the billing zone");
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
                .orElseThrow(() -> new NotFoundException("Premises not found", ErrorCode.NOT_FOUND));
        validateBillingZone(request.billingZoneId());
        validateParent(request.billingZoneId(), request.parentId(), id);
        if (premisesRepository.existsByBillingZoneIdAndPremisesCodeAndIdNot(
                request.billingZoneId(), request.premisesCode(), id)) {
            throw new BadRequestException("Premises code already exists in the billing zone");
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
                .orElseThrow(() -> new NotFoundException("Premises not found", ErrorCode.NOT_FOUND));
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

    @Transactional
    @Override
    public void delete(Long id) {
        Premises premises = premisesRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Premises not found", ErrorCode.NOT_FOUND));
        if (connectionRepository.existsByPremisesId(id)) {
            throw new BadRequestException("Premises is linked to a connection and cannot be deleted");
        }
        if (premisesRepository.existsByParentId(id)) {
            throw new BadRequestException("Premises has child entries and cannot be deleted");
        }
        premisesRepository.delete(premises);
    }

    private void validateBillingZone(Long billingZoneId) {
        if (billingZoneId == null || !billingZoneRepository.existsById(billingZoneId)) {
            throw new NotFoundException("Billing zone not found", ErrorCode.NOT_FOUND);
        }
    }

    private void validateParent(Long billingZoneId, Long parentId, Long currentId) {
        if (parentId == null) {
            return;
        }
        if (currentId != null && currentId.equals(parentId)) {
            throw new BadRequestException("Premises cannot reference itself as a parent");
        }
        Premises parent = premisesRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException("Parent premises not found", ErrorCode.NOT_FOUND));
        if (!parent.getBillingZoneId().equals(billingZoneId)) {
            throw new BadRequestException("Parent premises must belong to the same billing zone");
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
                premises.getUpdatedAt()
        );
    }
}
