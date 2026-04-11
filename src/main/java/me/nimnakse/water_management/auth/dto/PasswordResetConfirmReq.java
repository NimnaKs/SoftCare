package me.nimnakse.water_management.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordResetConfirmReq(
        @NotBlank String username,
        @NotBlank @Pattern(regexp = "\\d{6}", message = "OTP must be exactly 6 digits") String otp,
        @NotBlank String password,
        @NotBlank String confirmPassword
) {
}
