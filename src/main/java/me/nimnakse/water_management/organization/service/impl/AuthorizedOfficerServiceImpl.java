package me.nimnakse.water_management.organization.service.impl;

import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.organization.dto.request.AuthorizedOfficerCreateReq;
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
        if (!organizationRepository.existsById(organizationId)) {
            throw new NotFoundException("Organization not found", ErrorCode.NOT_FOUND);
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

    private AuthorizedOfficerRes toResponse(AuthorizedOfficer officer) {
        return new AuthorizedOfficerRes(officer.getId(), officer.getOrganizationId(), officer.getDesignation(),
                officer.getName(), officer.getNic(), officer.getMobileNumber(), officer.getStampPhotoUrl(),
                officer.getSignaturePhotoUrl(), officer.isActive());
    }
}
