package me.nimnakse.water_management.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshReq(
        @NotBlank String refreshToken
) {
}
