package me.nimnakse.watermanagement.users.dto.internal;

import me.nimnakse.watermanagement.users.entity.UserStatus;

public record UserMiniInternalDto(
        Long id,
        String name,
        Long orgUnitId,
        UserStatus status
) {
}
