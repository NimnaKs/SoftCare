package me.nimnakse.water_management.users.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserPasswordUpdateReq(
        @NotBlank String password
) {
}
