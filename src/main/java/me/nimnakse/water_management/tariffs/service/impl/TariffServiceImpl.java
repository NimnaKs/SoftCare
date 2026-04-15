package me.nimnakse.water_management.tariffs.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.connections.repository.ConnectionRepository;
import me.nimnakse.water_management.security.OrganizationAccessService;
import me.nimnakse.water_management.tariffs.dto.request.TariffCreateReq;
import me.nimnakse.water_management.tariffs.dto.request.TariffUpdateReq;
import me.nimnakse.water_management.tariffs.dto.request.TariffSlabReq;
import me.nimnakse.water_management.tariffs.dto.request.TariffLateFeeReq;
import me.nimnakse.water_management.tariffs.dto.response.TariffRes;
import me.nimnakse.water_management.tariffs.dto.response.TariffSlabRes;
import me.nimnakse.water_management.tariffs.dto.response.TariffLateFeeRes;
import me.nimnakse.water_management.tariffs.entity.Tariff;
import me.nimnakse.water_management.tariffs.entity.TariffSlab;
import me.nimnakse.water_management.tariffs.entity.TariffLateFee;
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
        populateEntity(tariff, request.name(), request.description(), orgUnitId,
                request.newConnectionFee(), request.reconnectionFee(), request.reconnectionCreditLimit(),
                request.meterDigits(), request.avgMonthlyMaxConsumption(), request.zeroConsumptionCharge(), request.chargingMethod(),
                request.slabs(), request.lateFees());

        return toResponse(tariffRepository.save(tariff));
    }

    @Transactional
    @Override
    public TariffRes update(Long id, TariffUpdateReq request) {
        Tariff tariff = tariffRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tariff not found", "ගාස්තු ක්‍රමය සොයාගත නොහැක",
                        ErrorCode.NOT_FOUND));

        Long orgUnitId = request.orgUnitId() != null ? request.orgUnitId() : tariff.getOrgUnitId();
        accessService.enforceOrgUnitAccess(orgUnitId);

        if (tariffRepository.existsByOrgUnitIdAndNameIgnoreCaseAndIdNot(orgUnitId, request.name(), id)) {
            throw new BadRequestException("Tariff already exists", "ගාස්තු ක්‍රමය දැනටමත් පවතී");
        }

        populateEntity(tariff, request.name(), request.description(), orgUnitId,
                request.newConnectionFee(), request.reconnectionFee(), request.reconnectionCreditLimit(),
                request.meterDigits(), request.avgMonthlyMaxConsumption(), request.zeroConsumptionCharge(), request.chargingMethod(),
                request.slabs(), request.lateFees());

        return toResponse(tariffRepository.save(tariff));
    }

    private void populateEntity(Tariff tariff, String name, String description, Long orgUnitId,
            Double newConnectionFee, Double reconnectionFee, Double reconnectionCreditLimit,
            Integer meterDigits, Double avgMonthlyMaxConsumption, Double zeroConsumptionCharge, String chargingMethod,
            List<TariffSlabReq> slabs, List<TariffLateFeeReq> lateFees) {

        tariff.setOrgUnitId(orgUnitId);
        tariff.setName(name.trim());
        tariff.setDescription(description != null ? description.trim() : null);
        tariff.setNewConnectionFee(newConnectionFee);
        tariff.setReconnectionFee(reconnectionFee);
        tariff.setReconnectionCreditLimit(reconnectionCreditLimit);
        tariff.setMeterDigits(meterDigits);
        tariff.setAvgMonthlyMaxConsumption(avgMonthlyMaxConsumption);
        tariff.setZeroConsumptionCharge(zeroConsumptionCharge);
        tariff.setChargingMethod(chargingMethod);

        // Manage Slabs
        tariff.getSlabs().clear();
        if (slabs != null) {
            for (TariffSlabReq slabReq : slabs) {
                TariffSlab slab = new TariffSlab();
                slab.setTariff(tariff);
                slab.setGap(slabReq.gap());
                slab.setFromUnit(slabReq.fromUnit());
                slab.setToUnit(slabReq.toUnit());
                slab.setCharge(slabReq.charge());
                slab.setRental(slabReq.rental());
                tariff.getSlabs().add(slab);
            }
        }

        // Manage Late Fees
        tariff.getLateFees().clear();
        if (lateFees != null) {
            for (TariffLateFeeReq FeeReq : lateFees) {
                TariffLateFee fee = new TariffLateFee();
                fee.setTariff(tariff);
                fee.setApplyNormalBill(FeeReq.applyNormalBill());
                fee.setApplyRedBill(FeeReq.applyRedBill());
                fee.setChargingMethod(FeeReq.chargingMethod());
                fee.setName(FeeReq.name());
                fee.setOverduePeriod(FeeReq.overduePeriod());
                fee.setLimitExceeded(FeeReq.limitExceeded());
                fee.setFixedAmount(FeeReq.fixedAmount());
                fee.setPercentage(FeeReq.percentage());
                tariff.getLateFees().add(fee);
            }
        }
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
        List<TariffSlabRes> slabResponses = tariff.getSlabs().stream()
                .map(s -> new TariffSlabRes(s.getId(), s.getGap(), s.getFromUnit(), s.getToUnit(), s.getCharge(),
                        s.getRental()))
                .collect(Collectors.toList());

        List<TariffLateFeeRes> lateFeeResponses = tariff.getLateFees().stream()
                .map(f -> new TariffLateFeeRes(f.getId(), f.isApplyNormalBill(), f.isApplyRedBill(),
                        f.getChargingMethod(), f.getName(), f.getOverduePeriod(), f.getLimitExceeded(),
                        f.getFixedAmount(), f.getPercentage()))
                .collect(Collectors.toList());

        return new TariffRes(
                tariff.getId(),
                tariff.getName(),
                tariff.getDescription(),
                tariff.getOrgUnitId(),
                tariff.getNewConnectionFee(),
                tariff.getReconnectionFee(),
                tariff.getReconnectionCreditLimit(),
                tariff.getMeterDigits(),
                tariff.getAvgMonthlyMaxConsumption(),
                tariff.getZeroConsumptionCharge(),
                tariff.getChargingMethod(),
                slabResponses,
                lateFeeResponses,
                tariff.getCreatedAt(),
                tariff.getUpdatedAt());
    }
}
