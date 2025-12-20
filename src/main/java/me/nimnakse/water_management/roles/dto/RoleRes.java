package me.nimnakse.water_management.roles.dto;

import me.nimnakse.water_management.roles.entity.RoleAppScope;

public record RoleRes(
        Long id,
        String name,
        String description,
        RoleAppScope appScope
) {
}
