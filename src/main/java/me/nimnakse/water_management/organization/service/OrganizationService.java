package me.nimnakse.water_management.organization.service;

import me.nimnakse.water_management.organization.dto.request.OrganizationCreateReq;
import me.nimnakse.water_management.organization.dto.request.OrganizationUpdateReq;
import me.nimnakse.water_management.organization.dto.response.OrganizationRes;

public interface OrganizationService {
    OrganizationRes create(OrganizationCreateReq request);

    OrganizationRes update(Long id, OrganizationUpdateReq request);

    OrganizationRes getById(Long id);
}
