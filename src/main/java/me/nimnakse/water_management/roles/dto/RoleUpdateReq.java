package me.nimnakse.water_management.roles.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import me.nimnakse.water_management.roles.entity.RoleAppScope;

public record RoleUpdateReq(
        @NotBlank String name,
        String description,
        @NotNull RoleAppScope appScope
) {
}
