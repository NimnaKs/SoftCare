package me.nimnakse.water_management.security;

import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.organization.repository.OrganizationRepository;
import org.springframework.stereotype.Service;

@Service
public class OrganizationAccessService {
    private final OrganizationRepository organizationRepository;

    public OrganizationAccessService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public Long resolveOrgUnitId() {
        Long organizationId = SecurityUtils.getOrganizationId();
        if (organizationId == null) {
            return null;
        }
        return organizationRepository.findByIdAndDeletedAtIsNull(organizationId)
                .map(org -> org.getOrgUnitId())
                .orElseThrow(() -> new NotFoundException("Organization not found", ErrorCode.NOT_FOUND));
    }
}
