package me.nimnakse.water_management.auth.controller;

import jakarta.validation.Valid;
import me.nimnakse.water_management.auth.dto.AuthRes;
import me.nimnakse.water_management.auth.dto.LoginReq;
import me.nimnakse.water_management.auth.dto.RefreshReq;
import me.nimnakse.water_management.auth.service.AuthService;
import me.nimnakse.water_management.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthRes>> login(@Valid @RequestBody LoginReq request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request.username(), request.password())));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthRes>> refresh(@Valid @RequestBody RefreshReq request) {
        return ResponseEntity.ok(ApiResponse.success(authService.refresh(request.refreshToken())));
    }
}
