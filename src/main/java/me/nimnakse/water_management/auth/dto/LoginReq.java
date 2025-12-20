package me.nimnakse.water_management.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginReq(
        @NotBlank String username,
        @NotBlank String password
) {
}
