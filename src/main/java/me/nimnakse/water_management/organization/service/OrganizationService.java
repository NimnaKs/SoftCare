package me.nimnakse.water_management.organization.service;

import me.nimnakse.water_management.organization.dto.request.OrganizationCreateReq;
import me.nimnakse.water_management.organization.dto.request.OrganizationUpdateReq;
import me.nimnakse.water_management.organization.dto.response.OrganizationRes;
import me.nimnakse.water_management.common.api.PageResponse;

public interface OrganizationService {
    OrganizationRes create(OrganizationCreateReq request);

    OrganizationRes update(Long id, OrganizationUpdateReq request);

    OrganizationRes getById(Long id);

    java.util.List<OrganizationRes> getAll();

    PageResponse<OrganizationRes> getAllPaginated(int page, int size);

    void delete(Long id);
}
