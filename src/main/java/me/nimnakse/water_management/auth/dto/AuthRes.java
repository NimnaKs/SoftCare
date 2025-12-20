package me.nimnakse.water_management.auth.dto;

public record AuthRes(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds
) {
}
