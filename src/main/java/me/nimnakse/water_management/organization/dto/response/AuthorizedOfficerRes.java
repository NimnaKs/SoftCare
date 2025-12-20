package me.nimnakse.water_management.organization.dto.response;

public record AuthorizedOfficerRes(
        Long id,
        Long organizationId,
        String designation,
        String name,
        String nic,
        String mobileNumber,
        String stampPhotoUrl,
        String signaturePhotoUrl,
        boolean isActive
) {
}
