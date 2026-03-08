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
import me.nimnakse.water_management.security.OrganizationAccessService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgencyServiceImpl implements AgencyService {
    private static final BigDecimal DEFAULT_SERVICE_CHARGE_PERCENT = new BigDecimal("15.00");
    private static final BigDecimal DEFAULT_SUBSCRIPTION_FEE = new BigDecimal("5.00");

    private final AgencyRepository agencyRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationAccessService organizationAccessService;

    public AgencyServiceImpl(
            AgencyRepository agencyRepository,
            OrganizationRepository organizationRepository,
            OrganizationAccessService organizationAccessService) {
        this.agencyRepository = agencyRepository;
        this.organizationRepository = organizationRepository;
        this.organizationAccessService = organizationAccessService;
    }

    @Transactional
    @Override
    public AgencyRes create(AgencyCreateReq request) {
        validateUniqueness(request.mobileNumber(), request.nicNumber(), null);
        Organization organization = resolveOrganizationByOrgId(request.organizationId());

        Agency agency = new Agency();
        applyFields(agency, request.businessName(), request.mobileNumber(),
                request.nicNumber(), request.businessAddress(), request.brcNumber(), request.ownerName(),
                request.secondaryContactNo(), request.serviceChargePercent(),
                request.subscriptionFee(), BigDecimal.ZERO, null, request.isActive());
        agency.setOrganization(organization);
        agency.setWalletAmount(BigDecimal.ZERO.setScale(2));

        return toResponse(agencyRepository.save(agency));
    }

    @Transactional
    @Override
    public AgencyRes update(Long id, AgencyUpdateReq request) {
        Agency agency = agencyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Agency not found", "නියෝජිත ආයතනය සොයාගත නොහැක",
                        ErrorCode.NOT_FOUND));

        validateUniqueness(request.mobileNumber(), request.nicNumber(), id);
        Organization organization = resolveOrganizationByOrgId(request.organizationId());

        applyFields(agency, request.businessName(), request.mobileNumber(),
                request.nicNumber(), request.businessAddress(), request.brcNumber(), request.ownerName(),
                request.secondaryContactNo(), request.serviceChargePercent(),
                request.subscriptionFee(), BigDecimal.ZERO, null, request.isActive());
        agency.setOrganization(organization);
        if (agency.getWalletAmount() == null) {
            agency.setWalletAmount(BigDecimal.ZERO.setScale(2));
        }

        return toResponse(agencyRepository.save(agency));
    }

    @Transactional(readOnly = true)
    @Override
    public AgencyRes getById(Long id) {
        Agency agency = agencyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Agency not found", "නියෝජිත ආයතනය සොයාගත නොහැක",
                        ErrorCode.NOT_FOUND));
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
    public List<AgencyRes> listByOrganizationId(Long organizationId) {
        Organization organization = resolveOrganizationByOrgId(organizationId);
        organizationAccessService.enforceOrgUnitAccess(organization.getOrgUnitId());
        return agencyRepository.findAllByOrganization_OrgUnitIdAndDeletedAtIsNull(organization.getOrgUnitId()).stream()
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
                .orElseThrow(() -> new NotFoundException("Agency not found", "නියෝජිත ආයතනය සොයාගත නොහැක",
                        ErrorCode.NOT_FOUND));
        agency.setDeletedAt(Instant.now());
        agencyRepository.save(agency);
    }

    private AgencyRes toResponse(Agency agency) {
        return new AgencyRes(
                agency.getId(),
                agency.getOrganization().getOrgUnitId(),
                agency.getBusinessName(),
                agency.getMobileNumber(),
                agency.getNicNumber(),
                agency.getBusinessAddress(),
                agency.getBrcNumber(),
                agency.getOwnerName(),
                agency.getSecondaryContactNo(),
                agency.getServiceChargePercent(),
                agency.getSubscriptionFee(),
                agency.getTotalCharges(),
                agency.getCreditLimit(),
                agency.getWalletAmount(),
                agency.getBillingMode(),
                agency.getIsActive(),
                agency.getCreatedAt(),
                agency.getUpdatedAt());
    }

    private void applyFields(Agency agency,
            String businessName,
            String mobileNumber,
            String nicNumber,
            String businessAddress,
            String brcNumber,
            String ownerName,
            String secondaryContactNo,
            BigDecimal serviceChargePercent,
            BigDecimal subscriptionFee,
            BigDecimal creditLimit,
            BillingMode billingMode,
            Boolean isActive) {
        agency.setBusinessName(businessName);
        agency.setMobileNumber(mobileNumber);
        agency.setNicNumber(nicNumber);
        agency.setBusinessAddress(businessAddress);
        agency.setBrcNumber(brcNumber);
        agency.setOwnerName(ownerName);
        agency.setSecondaryContactNo(secondaryContactNo);
        BigDecimal resolvedServiceChargePercent =
                serviceChargePercent != null ? serviceChargePercent : DEFAULT_SERVICE_CHARGE_PERCENT;
        BigDecimal resolvedSubscriptionFee =
                subscriptionFee != null ? subscriptionFee : DEFAULT_SUBSCRIPTION_FEE;
        agency.setServiceChargePercent(resolvedServiceChargePercent);
        agency.setSubscriptionFee(resolvedSubscriptionFee);
        agency.setTotalCharges(resolvedServiceChargePercent.add(resolvedSubscriptionFee));
        billingMode = BillingMode.PREPAID;
        creditLimit = BigDecimal.ZERO.setScale(2);
        BillingMode resolvedBillingMode = billingMode != null ? billingMode : BillingMode.PREPAID;
        agency.setBillingMode(resolvedBillingMode);
        if (resolvedBillingMode == BillingMode.PREPAID) {
            agency.setCreditLimit(BigDecimal.ZERO.setScale(2));
        } else {
            if (creditLimit == null) {
                throw new BadRequestException("Credit limit is required for postpaid billing mode",
                        "පසුගෙවුම් බිල්පත් ක්‍රමය සඳහා ණය සීමාව අනිවාර්යයි");
            }
            if (creditLimit.compareTo(BigDecimal.ZERO) >= 0) {
                throw new BadRequestException("Credit limit must be a negative value for postpaid billing mode",
                        "පසුගෙවුම් බිල්පත් ක්‍රමය සඳහා ණය සීමාව ඍණ අගයක් විය යුතුය");
            }
            agency.setCreditLimit(creditLimit);
        }
        agency.setIsActive(isActive != null ? isActive : Boolean.TRUE);
    }

    private void validateUniqueness(String mobileNumber, String nicNumber, Long currentId) {
        boolean mobileExists = currentId == null
                ? agencyRepository.existsByMobileNumberAndDeletedAtIsNull(mobileNumber)
                : agencyRepository.existsByMobileNumberAndIdNotAndDeletedAtIsNull(mobileNumber, currentId);
        if (mobileExists) {
            throw new BadRequestException("Mobile number already exists", "ජංගම දුරකථන අංකය දැනටමත් පවතී");
        }

        boolean nicExists = currentId == null
                ? agencyRepository.existsByNicNumberAndDeletedAtIsNull(nicNumber)
                : agencyRepository.existsByNicNumberAndIdNotAndDeletedAtIsNull(nicNumber, currentId);
        if (nicExists) {
            throw new BadRequestException("NIC number already exists", "ජාතික හැඳුනුම්පත් අංකය දැනටමත් පවතී");
        }
    }

    private Organization resolveOrganizationByOrgId(Long organizationId) {
        return organizationRepository.findByOrgUnitIdAndDeletedAtIsNull(organizationId)
                .or(() -> organizationRepository.findByIdAndDeletedAtIsNull(organizationId))
                .orElseThrow(() -> new NotFoundException("Organization not found", "සංවිධානය සොයාගත නොහැක",
                        ErrorCode.NOT_FOUND));
    }
}
