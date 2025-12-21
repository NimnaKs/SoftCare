package me.nimnakse.water_management.organization.service.impl;

import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import java.time.Instant;
import me.nimnakse.water_management.organization.dto.request.OrganizationCreateReq;
import me.nimnakse.water_management.organization.dto.request.OrganizationUpdateReq;
import me.nimnakse.water_management.organization.dto.response.OrganizationRes;
import me.nimnakse.water_management.organization.entity.OrgUnit;
import me.nimnakse.water_management.organization.entity.OrgUnitLevel;
import me.nimnakse.water_management.organization.entity.Organization;
import me.nimnakse.water_management.organization.repository.OrgUnitRepository;
import me.nimnakse.water_management.organization.repository.OrganizationRepository;
import me.nimnakse.water_management.organization.service.OrganizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationServiceImpl implements OrganizationService {
    private static final long MIN_ORGANIZATION_CODE = 400117L;

    private final OrganizationRepository organizationRepository;
    private final OrgUnitRepository orgUnitRepository;

    public OrganizationServiceImpl(OrganizationRepository organizationRepository,
                                   OrgUnitRepository orgUnitRepository) {
        this.organizationRepository = organizationRepository;
        this.orgUnitRepository = orgUnitRepository;
    }

    @Transactional
    @Override
    public OrganizationRes create(OrganizationCreateReq request) {
        OrgUnit orgUnit = orgUnitRepository.findById(request.orgUnitId())
                .orElseThrow(() -> new NotFoundException("Org unit not found", ErrorCode.NOT_FOUND));
        if (orgUnit.getLevel() != OrgUnitLevel.BRANCH) {
            throw new BadRequestException("Organizations can only be created for branch org units");
        }
        if (organizationRepository.findByOrgUnitIdAndDeletedAtIsNull(request.orgUnitId()).isPresent()) {
            throw new BadRequestException("Organization already exists for this org unit");
        }

        Organization organization = new Organization();
        organization.setOrgUnitId(request.orgUnitId());
        organization.setOrganizationCode(nextOrganizationCode());
        applyOrganizationFields(organization, request.nameEn(), request.nameSi(), request.nameTa(),
                request.addressEn(), request.addressSi(), request.addressTa(), request.postalCode(),
                request.registrationNumber(), request.email(), request.mobileNumber(),
                request.telephoneNumber(), request.logoUrl());

        Organization saved = organizationRepository.save(organization);
        return toResponse(saved);
    }

    @Transactional
    @Override
    public OrganizationRes update(Long id, OrganizationUpdateReq request) {
        Organization organization = organizationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Organization not found", ErrorCode.NOT_FOUND));
        if (!organization.getOrgUnitId().equals(request.orgUnitId())) {
            OrgUnit orgUnit = orgUnitRepository.findById(request.orgUnitId())
                    .orElseThrow(() -> new NotFoundException("Org unit not found", ErrorCode.NOT_FOUND));
            if (orgUnit.getLevel() != OrgUnitLevel.BRANCH) {
                throw new BadRequestException("Organizations can only be created for branch org units");
            }
            organizationRepository.findByOrgUnitIdAndDeletedAtIsNull(request.orgUnitId())
                    .filter(existing -> !existing.getId().equals(organization.getId()))
                    .ifPresent(existing -> {
                        throw new BadRequestException("Organization already exists for this org unit");
                    });
            organization.setOrgUnitId(request.orgUnitId());
        }
        applyOrganizationFields(organization, request.nameEn(), request.nameSi(), request.nameTa(),
                request.addressEn(), request.addressSi(), request.addressTa(), request.postalCode(),
                request.registrationNumber(), request.email(), request.mobileNumber(),
                request.telephoneNumber(), request.logoUrl());
        return toResponse(organization);
    }

    @Transactional(readOnly = true)
    @Override
    public OrganizationRes getById(Long id) {
        Organization organization = organizationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Organization not found", ErrorCode.NOT_FOUND));
        return toResponse(organization);
    }

    @Transactional(readOnly = true)
    @Override
    public java.util.List<OrganizationRes> getAll() {
        return organizationRepository.findAllByDeletedAtIsNull().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Organization organization = organizationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Organization not found", ErrorCode.NOT_FOUND));
        organization.setDeletedAt(Instant.now());
    }

    private void applyOrganizationFields(Organization organization,
                                         String nameEn,
                                         String nameSi,
                                         String nameTa,
                                         String addressEn,
                                         String addressSi,
                                         String addressTa,
                                         String postalCode,
                                         String registrationNumber,
                                         String email,
                                         String mobileNumber,
                                         String telephoneNumber,
                                         String logoUrl) {
        organization.setNameEn(nameEn);
        organization.setNameSi(nameSi);
        organization.setNameTa(nameTa);
        organization.setAddressEn(addressEn);
        organization.setAddressSi(addressSi);
        organization.setAddressTa(addressTa);
        organization.setPostalCode(postalCode);
        organization.setRegistrationNumber(registrationNumber);
        organization.setEmail(email);
        organization.setMobileNumber(mobileNumber);
        organization.setTelephoneNumber(telephoneNumber);
        organization.setLogoUrl(logoUrl);
    }

    private OrganizationRes toResponse(Organization organization) {
        return new OrganizationRes(organization.getId(), organization.getOrgUnitId(),
                organization.getOrganizationCode(), organization.getNameEn(), organization.getNameSi(),
                organization.getNameTa(), organization.getAddressEn(), organization.getAddressSi(),
                organization.getAddressTa(), organization.getPostalCode(), organization.getRegistrationNumber(),
                organization.getEmail(), organization.getMobileNumber(), organization.getTelephoneNumber(),
                organization.getLogoUrl());
    }

    private String nextOrganizationCode() {
        Long maxCode = organizationRepository.findMaxOrganizationCode();
        long next = maxCode == null ? MIN_ORGANIZATION_CODE : Math.max(maxCode + 1, MIN_ORGANIZATION_CODE);
        return String.valueOf(next);
    }
}
