package me.nimnakse.water_management.users.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.users.dto.request.UserCreateReq;
import me.nimnakse.water_management.users.dto.request.UserPasswordUpdateReq;
import me.nimnakse.water_management.users.dto.request.UserUpdateReq;
import me.nimnakse.water_management.users.dto.response.UserRes;
import me.nimnakse.water_management.users.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User management operations")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Create user", description = "Creates a new user with assigned roles.")
    public ResponseEntity<ApiResponse<UserRes>> createUser(@Valid @RequestBody UserCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(userService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates user profile and role assignments.")
    public ResponseEntity<ApiResponse<UserRes>> updateUser(@PathVariable Long id,
                                                           @Valid @RequestBody UserUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(userService.update(id, request)));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate user", description = "Deactivates a user account.")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        userService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("User deactivated", null));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate user", description = "Activates a user account.")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id) {
        userService.activate(id);
        return ResponseEntity.ok(ApiResponse.success("User activated", null));
    }

    @PatchMapping("/{id}/password")
    @Operation(summary = "Update user password", description = "Updates the password for a user account.")
    public ResponseEntity<ApiResponse<Void>> updatePassword(@PathVariable Long id,
                                                            @Valid @RequestBody UserPasswordUpdateReq request) {
        userService.updatePassword(id, request);
        return ResponseEntity.ok(ApiResponse.success("Password updated", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user", description = "Fetches a user by identifier.")
    public ResponseEntity<ApiResponse<UserRes>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getById(id)));
    }
}
