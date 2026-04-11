package me.nimnakse.water_management.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.nimnakse.water_management.auth.dto.PasswordResetConfirmReq;
import me.nimnakse.water_management.auth.dto.PasswordResetOtpReq;
import me.nimnakse.water_management.auth.service.PasswordResetService;
import me.nimnakse.water_management.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/password-reset")
@Tag(name = "Password Reset", description = "Username based password reset flow")
@CrossOrigin
public class PasswordResetController {
    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/request-otp")
    @Operation(summary = "Request password reset OTP", description = "Looks up the user by username and sends an OTP to the registered mobile number.")
    public ResponseEntity<ApiResponse<Void>> requestOtp(@Valid @RequestBody PasswordResetOtpReq request) {
        passwordResetService.requestOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP sent", null));
    }

    @PostMapping("/reset")
    @Operation(summary = "Confirm password reset", description = "Validates the OTP and updates the user password.")
    public ResponseEntity<ApiResponse<Void>> confirmReset(@Valid @RequestBody PasswordResetConfirmReq request) {
        passwordResetService.confirmReset(request);
        return ResponseEntity.ok(ApiResponse.success("Password updated", null));
    }
}
