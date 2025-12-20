package me.nimnakse.watermanagement.users.controller;

import jakarta.validation.Valid;
import me.nimnakse.watermanagement.common.api.ApiResponse;
import me.nimnakse.watermanagement.users.dto.request.UserCreateReq;
import me.nimnakse.watermanagement.users.dto.request.UserUpdateReq;
import me.nimnakse.watermanagement.users.dto.response.UserRes;
import me.nimnakse.watermanagement.users.service.UserService;
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
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserRes>> createUser(@Valid @RequestBody UserCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(userService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserRes>> updateUser(@PathVariable Long id,
                                                           @Valid @RequestBody UserUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(userService.update(id, request)));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        userService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("User deactivated", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserRes>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getById(id)));
    }
}
