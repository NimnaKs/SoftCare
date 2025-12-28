package me.nimnakse.water_management.agencies.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import me.nimnakse.water_management.agencies.dto.request.AgencyCreateReq;
import me.nimnakse.water_management.agencies.dto.request.AgencyUpdateReq;
import me.nimnakse.water_management.agencies.dto.response.AgencyRes;
import me.nimnakse.water_management.agencies.entity.Agency;
import me.nimnakse.water_management.agencies.entity.BillingMode;
import me.nimnakse.water_management.agencies.repository.AgencyRepository;
import me.nimnakse.water_management.agencies.service.AgencyService;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.organization.entity.Organization;
import me.nimnakse.water_management.organization.repository.OrganizationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgencyServiceImpl implements AgencyService {
    private static final BigDecimal DEFAULT_SERVICE_CHARGE_PERCENT = new BigDecimal("15.00");
    private static final BigDecimal DEFAULT_SUBSCRIPTION_FEE = new BigDecimal("5.00");
    private static final BigDecimal DEFAULT_TOTAL_CHARGES = new BigDecimal("20.00");

    private final AgencyRepository agencyRepository;
    private final OrganizationRepository organizationRepository;

    public AgencyServiceImpl(AgencyRepository agencyRepository, OrganizationRepository organizationRepository) {
        this.agencyRepository = agencyRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    @Override
    public AgencyRes create(AgencyCreateReq request) {
        validateUniqueness(request.mobileNumber(), request.nicNumber(), null);
        Organization organization = resolveOrganization(request.organizationId());

        Agency agency = new Agency();
        applyFields(agency, request.businessName(), request.mobileNumber(),
                request.nicNumber(), request.businessAddress(), request.brcNumber(), request.ownerName(),
                request.ownerNicNumber(), request.secondaryContactNo(), request.serviceChargePercent(),
                request.subscriptionFee(), request.totalCharges(), request.billingMode(), request.isActive());
        agency.setOrganization(organization);

        return toResponse(agencyRepository.save(agency));
    }

    @Transactional
    @Override
    public AgencyRes update(Long id, AgencyUpdateReq request) {
        Agency agency = agencyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Agency not found", ErrorCode.NOT_FOUND));

        validateUniqueness(request.mobileNumber(), request.nicNumber(), id);
        Organization organization = resolveOrganization(request.organizationId());

        applyFields(agency, request.businessName(), request.mobileNumber(),
                request.nicNumber(), request.businessAddress(), request.brcNumber(), request.ownerName(),
                request.ownerNicNumber(), request.secondaryContactNo(), request.serviceChargePercent(),
                request.subscriptionFee(), request.totalCharges(), request.billingMode(), request.isActive());
        agency.setOrganization(organization);

        return toResponse(agencyRepository.save(agency));
    }

    @Transactional(readOnly = true)
    @Override
    public AgencyRes getById(Long id) {
        Agency agency = agencyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Agency not found", ErrorCode.NOT_FOUND));
        return toResponse(agency);
    }

    @Transactional(readOnly = true)
    @Override
    public List<AgencyRes> list() {
        return agencyRepository.findAllByDeletedAtIsNull().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<AgencyRes> listPaginated(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<Agency> agencyPage = agencyRepository.findAllByDeletedAtIsNull(pageRequest);
        List<AgencyRes> items = agencyPage.getContent().stream()
                .map(this::toResponse)
                .toList();
        return new PageResponse<>(items, agencyPage.getTotalElements(), agencyPage.getTotalPages(),
                agencyPage.getNumber(), agencyPage.getSize());
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Agency agency = agencyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Agency not found", ErrorCode.NOT_FOUND));
        agency.setDeletedAt(Instant.now());
        agencyRepository.save(agency);
    }

    private AgencyRes toResponse(Agency agency) {
        return new AgencyRes(
                agency.getId(),
                agency.getOrganization().getId(),
                agency.getBusinessName(),
                agency.getMobileNumber(),
                agency.getNicNumber(),
                agency.getBusinessAddress(),
                agency.getBrcNumber(),
                agency.getOwnerName(),
                agency.getOwnerNicNumber(),
                agency.getSecondaryContactNo(),
                agency.getServiceChargePercent(),
                agency.getSubscriptionFee(),
                agency.getTotalCharges(),
                agency.getBillingMode(),
                agency.getIsActive(),
                agency.getCreatedAt(),
                agency.getUpdatedAt()
        );
    }

    private void applyFields(Agency agency,
                             String businessName,
                             String mobileNumber,
                             String nicNumber,
                             String businessAddress,
                             String brcNumber,
                             String ownerName,
                             String ownerNicNumber,
                             String secondaryContactNo,
                             BigDecimal serviceChargePercent,
                             BigDecimal subscriptionFee,
                             BigDecimal totalCharges,
                             BillingMode billingMode,
                             Boolean isActive) {
        agency.setBusinessName(businessName);
        agency.setMobileNumber(mobileNumber);
        agency.setNicNumber(nicNumber);
        agency.setBusinessAddress(businessAddress);
        agency.setBrcNumber(brcNumber);
        agency.setOwnerName(ownerName);
        agency.setOwnerNicNumber(ownerNicNumber);
        agency.setSecondaryContactNo(secondaryContactNo);
        agency.setServiceChargePercent(serviceChargePercent != null ? serviceChargePercent : DEFAULT_SERVICE_CHARGE_PERCENT);
        agency.setSubscriptionFee(subscriptionFee != null ? subscriptionFee : DEFAULT_SUBSCRIPTION_FEE);
        agency.setTotalCharges(totalCharges != null ? totalCharges : DEFAULT_TOTAL_CHARGES);
        agency.setBillingMode(billingMode != null ? billingMode : BillingMode.PREPAID);
        agency.setIsActive(isActive != null ? isActive : Boolean.TRUE);
    }

    private void validateUniqueness(String mobileNumber, String nicNumber, Long currentId) {
        boolean mobileExists = currentId == null
                ? agencyRepository.existsByMobileNumberAndDeletedAtIsNull(mobileNumber)
                : agencyRepository.existsByMobileNumberAndIdNotAndDeletedAtIsNull(mobileNumber, currentId);
        if (mobileExists) {
            throw new BadRequestException("Mobile number already exists");
        }

        boolean nicExists = currentId == null
                ? agencyRepository.existsByNicNumberAndDeletedAtIsNull(nicNumber)
                : agencyRepository.existsByNicNumberAndIdNotAndDeletedAtIsNull(nicNumber, currentId);
        if (nicExists) {
            throw new BadRequestException("NIC number already exists");
        }
    }

    private Organization resolveOrganization(Long organizationId) {
        return organizationRepository.findByIdAndDeletedAtIsNull(organizationId)
                .orElseThrow(() -> new NotFoundException("Organization not found", ErrorCode.NOT_FOUND));
    }
}
