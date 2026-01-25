package me.nimnakse.water_management.organization.service.impl;

import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import java.time.Instant;
import java.util.List;
import me.nimnakse.water_management.organization.dto.request.OrgNotificationContactCreateReq;
import me.nimnakse.water_management.organization.dto.request.OrgNotificationContactUpdateReq;
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
        if (!organizationRepository.existsByIdAndDeletedAtIsNull(organizationId)) {
            throw new NotFoundException("Organization not found", "සංවිධානය සොයාගත නොහැක", ErrorCode.NOT_FOUND);
        }

        OrgNotificationContact contact = new OrgNotificationContact();
        contact.setOrganizationId(organizationId);
        contact.setName(request.name());
        contact.setMobileNumber(request.mobileNumber());
        contact.setPriorityOrder(request.priorityOrder());

        OrgNotificationContact saved = contactRepository.save(contact);
        return toResponse(saved);
    }

    @Transactional
    @Override
    public OrgNotificationContactRes update(Long organizationId, Long contactId,
            OrgNotificationContactUpdateReq request) {
        OrgNotificationContact contact = getContactForOrg(organizationId, contactId);
        contact.setName(request.name());
        contact.setMobileNumber(request.mobileNumber());
        contact.setPriorityOrder(request.priorityOrder());
        return toResponse(contact);
    }

    @Transactional(readOnly = true)
    @Override
    public OrgNotificationContactRes getById(Long organizationId, Long contactId) {
        OrgNotificationContact contact = getContactForOrg(organizationId, contactId);
        return toResponse(contact);
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrgNotificationContactRes> getAll(Long organizationId) {
        if (!organizationRepository.existsByIdAndDeletedAtIsNull(organizationId)) {
            throw new NotFoundException("Organization not found", "සංවිධානය සොයාගත නොහැක", ErrorCode.NOT_FOUND);
        }
        return contactRepository.findByOrganizationIdAndDeletedAtIsNull(organizationId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long organizationId, Long contactId) {
        OrgNotificationContact contact = getContactForOrg(organizationId, contactId);
        contact.setDeletedAt(Instant.now());
    }

    private OrgNotificationContactRes toResponse(OrgNotificationContact contact) {
        return new OrgNotificationContactRes(contact.getId(), contact.getOrganizationId(), contact.getName(),
                contact.getMobileNumber(), contact.getPriorityOrder());
    }

    private OrgNotificationContact getContactForOrg(Long organizationId, Long contactId) {
        OrgNotificationContact contact = contactRepository.findByIdAndDeletedAtIsNull(contactId)
                .orElseThrow(() -> new NotFoundException("Authorized officer not found",
                        "බලයලත් නිලධාරියා සොයාගත නොහැක", ErrorCode.NOT_FOUND));
        if (!contact.getOrganizationId().equals(organizationId)) {
            throw new NotFoundException("Notification contact not found", "දැනුම්දීම් සම්බන්ධතාව සොයාගත නොහැක",
                    ErrorCode.NOT_FOUND);
        }
        return contact;
    }
}
