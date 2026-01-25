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
        Long orgUnitId = SecurityUtils.getOrganizationId();
        if (orgUnitId != null && isRestrictedAppScope()) {
            OrgUnit unit = orgUnitRepository.findById(orgUnitId)
                    .orElseThrow(() -> new NotFoundException("Org unit not found", "ආයතන ඒකකය හමු නොවීය",
                            ErrorCode.NOT_FOUND));
            if (!Boolean.TRUE.equals(unit.getIsActive())) {
                throw new me.nimnakse.water_management.common.exception.BadRequestException(
                        "Organization is deactivated",
                        "ආයතනය අක්‍රිය කර ඇත",
                        ErrorCode.ORG_DEACTIVATED);
            }
        }
        return orgUnitId;
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

        if (isRestrictedAppScope()) {
            checkHierarchyActive(target);
        }
    }

    private boolean isRestrictedAppScope() {
        return SecurityUtils.hasAnyAppScope(
                me.nimnakse.water_management.roles.entity.RoleAppScope.BRANCH_APP,
                me.nimnakse.water_management.roles.entity.RoleAppScope.AGENCY_APP,
                me.nimnakse.water_management.roles.entity.RoleAppScope.METER_APP);
    }

    private void checkHierarchyActive(OrgUnit target) {
        OrgUnit current = target;
        while (current != null) {
            if (!Boolean.TRUE.equals(current.getIsActive())) {
                throw new me.nimnakse.water_management.common.exception.BadRequestException(
                        "Organization in hierarchy is deactivated: " + current.getName(),
                        "ආයතන පද්ධතියේ ඒකකයක් අක්‍රිය කර ඇත: " + current.getName(),
                        ErrorCode.ORG_DEACTIVATED);
            }
            Long parentId = current.getParentId();
            if (parentId == null)
                break;
            current = orgUnitRepository.findById(parentId).orElse(null);
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
