package me.nimnakse.water_management.organization.service.impl;

import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.organization.dto.request.OrgNotificationContactCreateReq;
import me.nimnakse.water_management.organization.dto.response.OrgNotificationContactRes;
import me.nimnakse.water_management.organization.entity.OrgNotificationContact;
import me.nimnakse.water_management.organization.repository.OrgNotificationContactRepository;
import me.nimnakse.water_management.organization.repository.OrganizationRepository;
import me.nimnakse.water_management.organization.service.OrgNotificationContactService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrgNotificationContactServiceImpl implements OrgNotificationContactService {
    private final OrgNotificationContactRepository contactRepository;
    private final OrganizationRepository organizationRepository;

    public OrgNotificationContactServiceImpl(OrgNotificationContactRepository contactRepository,
                                             OrganizationRepository organizationRepository) {
        this.contactRepository = contactRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    @Override
    public OrgNotificationContactRes create(Long organizationId, OrgNotificationContactCreateReq request) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new NotFoundException("Organization not found", ErrorCode.NOT_FOUND);
        }

        OrgNotificationContact contact = new OrgNotificationContact();
        contact.setOrganizationId(organizationId);
        contact.setName(request.name());
        contact.setMobileNumber(request.mobileNumber());
        contact.setPriorityOrder(request.priorityOrder());

        OrgNotificationContact saved = contactRepository.save(contact);
        return toResponse(saved);
    }

    private OrgNotificationContactRes toResponse(OrgNotificationContact contact) {
        return new OrgNotificationContactRes(contact.getId(), contact.getOrganizationId(), contact.getName(),
                contact.getMobileNumber(), contact.getPriorityOrder());
    }
}
