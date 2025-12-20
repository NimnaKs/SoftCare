package me.nimnakse.watermanagement.roles.dto;

import me.nimnakse.watermanagement.roles.entity.RoleAppScope;

public record RoleRes(
        Long id,
        String name,
        String description,
        RoleAppScope appScope
) {
}
