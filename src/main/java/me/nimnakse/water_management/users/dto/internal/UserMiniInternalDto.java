package me.nimnakse.water_management.users.dto.internal;

import me.nimnakse.water_management.users.entity.UserStatus;

public record UserMiniInternalDto(
        Long id,
        String name,
        Long orgUnitId,
        UserStatus status
) {
}
