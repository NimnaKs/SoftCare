package me.nimnakse.water_management.organization.service;

import me.nimnakse.water_management.organization.dto.request.OrgNotificationContactCreateReq;
import me.nimnakse.water_management.organization.dto.request.OrgNotificationContactUpdateReq;
import me.nimnakse.water_management.organization.dto.response.OrgNotificationContactRes;

import java.util.List;

public interface OrgNotificationContactService {
    OrgNotificationContactRes create(Long organizationId, OrgNotificationContactCreateReq request);

    OrgNotificationContactRes update(Long organizationId, Long contactId, OrgNotificationContactUpdateReq request);

    OrgNotificationContactRes getById(Long organizationId, Long contactId);

    List<OrgNotificationContactRes> getAll(Long organizationId);

    void delete(Long organizationId, Long contactId);
}
