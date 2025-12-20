package me.nimnakse.water_management.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.nimnakse.water_management.auth.dto.AuthRes;
import me.nimnakse.water_management.auth.dto.LoginReq;
import me.nimnakse.water_management.auth.dto.RefreshReq;
import me.nimnakse.water_management.auth.service.AuthService;
import me.nimnakse.water_management.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication and token management")
@CrossOrigin
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates a user and returns access/refresh tokens.")
    public ResponseEntity<ApiResponse<AuthRes>> login(@Valid @RequestBody LoginReq request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request.username(), request.password())));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Issues new tokens using a refresh token.")
    public ResponseEntity<ApiResponse<AuthRes>> refresh(@Valid @RequestBody RefreshReq request) {
        return ResponseEntity.ok(ApiResponse.success(authService.refresh(request.refreshToken())));
    }
}
