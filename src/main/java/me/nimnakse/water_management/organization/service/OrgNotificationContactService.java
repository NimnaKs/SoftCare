package me.nimnakse.water_management.organization.service;

import me.nimnakse.water_management.organization.dto.request.OrgNotificationContactCreateReq;
import me.nimnakse.water_management.organization.dto.response.OrgNotificationContactRes;

public interface OrgNotificationContactService {
    OrgNotificationContactRes create(Long organizationId, OrgNotificationContactCreateReq request);
}
