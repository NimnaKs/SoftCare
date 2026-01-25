package me.nimnakse.water_management.organization.service.impl;

import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import java.time.Instant;
import java.util.List;
import me.nimnakse.water_management.organization.dto.request.AuthorizedOfficerCreateReq;
import me.nimnakse.water_management.organization.dto.request.AuthorizedOfficerUpdateReq;
import me.nimnakse.water_management.organization.dto.response.AuthorizedOfficerRes;
import me.nimnakse.water_management.organization.entity.AuthorizedOfficer;
import me.nimnakse.water_management.organization.repository.AuthorizedOfficerRepository;
import me.nimnakse.water_management.organization.repository.OrganizationRepository;
import me.nimnakse.water_management.organization.service.AuthorizedOfficerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthorizedOfficerServiceImpl implements AuthorizedOfficerService {
    private final AuthorizedOfficerRepository authorizedOfficerRepository;
    private final OrganizationRepository organizationRepository;

    public AuthorizedOfficerServiceImpl(AuthorizedOfficerRepository authorizedOfficerRepository,
            OrganizationRepository organizationRepository) {
        this.authorizedOfficerRepository = authorizedOfficerRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    @Override
    public AuthorizedOfficerRes create(Long organizationId, AuthorizedOfficerCreateReq request) {
        if (!organizationRepository.existsByIdAndDeletedAtIsNull(organizationId)) {
            throw new NotFoundException("Organization not found", "සංවිධානය සොයාගත නොහැක", ErrorCode.NOT_FOUND);
        }
        AuthorizedOfficer officer = new AuthorizedOfficer();
        officer.setOrganizationId(organizationId);
        officer.setDesignation(request.designation());
        officer.setName(request.name());
        officer.setNic(request.nic());
        officer.setMobileNumber(request.mobileNumber());
        officer.setStampPhotoUrl(request.stampPhotoUrl());
        officer.setSignaturePhotoUrl(request.signaturePhotoUrl());

        AuthorizedOfficer saved = authorizedOfficerRepository.save(officer);
        return toResponse(saved);
    }

    @Transactional
    @Override
    public AuthorizedOfficerRes update(Long organizationId, Long officerId, AuthorizedOfficerUpdateReq request) {
        AuthorizedOfficer officer = getOfficerForOrg(organizationId, officerId);
        officer.setDesignation(request.designation());
        officer.setName(request.name());
        officer.setNic(request.nic());
        officer.setMobileNumber(request.mobileNumber());
        officer.setStampPhotoUrl(request.stampPhotoUrl());
        officer.setSignaturePhotoUrl(request.signaturePhotoUrl());
        officer.setActive(request.active());
        return toResponse(officer);
    }

    @Transactional(readOnly = true)
    @Override
    public AuthorizedOfficerRes getById(Long organizationId, Long officerId) {
        AuthorizedOfficer officer = getOfficerForOrg(organizationId, officerId);
        return toResponse(officer);
    }

    @Transactional(readOnly = true)
    @Override
    public List<AuthorizedOfficerRes> getAll(Long organizationId) {
        if (!organizationRepository.existsByIdAndDeletedAtIsNull(organizationId)) {
            throw new NotFoundException("Organization not found", "සංවිධානය සොයාගත නොහැක", ErrorCode.NOT_FOUND);
        }
        return authorizedOfficerRepository.findByOrganizationIdAndDeletedAtIsNull(organizationId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long organizationId, Long officerId) {
        AuthorizedOfficer officer = getOfficerForOrg(organizationId, officerId);
        officer.setDeletedAt(Instant.now());
    }

    private AuthorizedOfficerRes toResponse(AuthorizedOfficer officer) {
        return new AuthorizedOfficerRes(officer.getId(), officer.getOrganizationId(), officer.getDesignation(),
                officer.getName(), officer.getNic(), officer.getMobileNumber(), officer.getStampPhotoUrl(),
                officer.getSignaturePhotoUrl(), officer.isActive());
    }

    private AuthorizedOfficer getOfficerForOrg(Long organizationId, Long officerId) {
        AuthorizedOfficer officer = authorizedOfficerRepository.findByIdAndDeletedAtIsNull(officerId)
                .orElseThrow(() -> new NotFoundException("Liability account not found", "වගකීම් ගිණුම සොයාගත නොහැක",
                        ErrorCode.NOT_FOUND));
        if (!officer.getOrganizationId().equals(organizationId)) {
            throw new NotFoundException("Authorized officer not found", "බලයලත් නිලධාරියා සොයාගත නොහැක",
                    ErrorCode.NOT_FOUND);
        }
        return officer;
    }
}
