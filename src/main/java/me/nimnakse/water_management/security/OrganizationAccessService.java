package me.nimnakse.water_management.security;

import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.organization.entity.OrgUnit;
import me.nimnakse.water_management.organization.repository.OrgUnitRepository;
import org.springframework.stereotype.Service;

@Service
public class OrganizationAccessService {
    private final OrgUnitRepository orgUnitRepository;

    public OrganizationAccessService(OrgUnitRepository orgUnitRepository) {
        this.orgUnitRepository = orgUnitRepository;
    }

    public Long resolveOrgUnitId() {
        return SecurityUtils.getOrganizationId();
    }

    public void enforceOrgUnitAccess(Long targetOrgUnitId) {
        if (targetOrgUnitId == null) {
            throw new NotFoundException("Org unit not found", "ආයතන ඒකකය හමු නොවීය", ErrorCode.NOT_FOUND);
        }

        Long rootOrgUnitId = resolveOrgUnitId();
        if (rootOrgUnitId == null) {
            return; // System admin or internal call
        }

        OrgUnit target = orgUnitRepository.findById(targetOrgUnitId)
                .orElseThrow(
                        () -> new NotFoundException("Org unit not found", "ආයතන ඒකකය හමු නොවීය", ErrorCode.NOT_FOUND));

        if (!isWithinHierarchy(rootOrgUnitId, target)) {
            throw new NotFoundException("Org unit not found", "ආයතන ඒකකය හමු නොවීය", ErrorCode.NOT_FOUND);
        }
    }

    private boolean isWithinHierarchy(Long rootOrgUnitId, OrgUnit target) {
        OrgUnit current = target;
        while (current != null) {
            if (current.getId().equals(rootOrgUnitId)) {
                return true;
            }
            Long parentId = current.getParentId();
            if (parentId == null) {
                return false;
            }
            current = orgUnitRepository.findById(parentId)
                    .orElseThrow(() -> new NotFoundException("Org unit not found", "ආයතන ඒකකය හමු නොවීය",
                            ErrorCode.NOT_FOUND));
        }
        return false;
    }
}
