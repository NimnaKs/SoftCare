package me.nimnakse.water_management.organization.service;

import me.nimnakse.water_management.organization.dto.request.AuthorizedOfficerCreateReq;
import me.nimnakse.water_management.organization.dto.response.AuthorizedOfficerRes;

public interface AuthorizedOfficerService {
    AuthorizedOfficerRes create(Long organizationId, AuthorizedOfficerCreateReq request);
}
