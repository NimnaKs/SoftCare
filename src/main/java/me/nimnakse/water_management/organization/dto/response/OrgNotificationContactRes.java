package me.nimnakse.water_management.organization.dto.response;

public record OrgNotificationContactRes(
        Long id,
        Long organizationId,
        String name,
        String mobileNumber,
        Integer priorityOrder
) {
}
