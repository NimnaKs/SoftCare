package me.nimnakse.water_management.organization.service;

import me.nimnakse.water_management.organization.dto.request.AuthorizedOfficerCreateReq;
import me.nimnakse.water_management.organization.dto.request.AuthorizedOfficerUpdateReq;
import me.nimnakse.water_management.organization.dto.response.AuthorizedOfficerRes;

import java.util.List;

public interface AuthorizedOfficerService {
    AuthorizedOfficerRes create(Long organizationId, AuthorizedOfficerCreateReq request);

    AuthorizedOfficerRes update(Long organizationId, Long officerId, AuthorizedOfficerUpdateReq request);

    AuthorizedOfficerRes getById(Long organizationId, Long officerId);

    List<AuthorizedOfficerRes> getAll(Long organizationId);

    void delete(Long organizationId, Long officerId);
}
