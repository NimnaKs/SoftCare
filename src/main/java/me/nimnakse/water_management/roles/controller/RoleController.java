package me.nimnakse.water_management.roles.controller;

import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.roles.dto.RoleCreateReq;
import me.nimnakse.water_management.roles.dto.RoleRes;
import me.nimnakse.water_management.roles.dto.RoleUpdateReq;
import me.nimnakse.water_management.roles.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/roles")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ApiResponse<List<RoleRes>> listRoles() {
        return ApiResponse.success(roleService.getRoles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleRes>> getRole(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(roleService.getRole(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleRes>> createRole(@Valid @RequestBody RoleCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(roleService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleRes>> updateRole(@PathVariable Long id,
                                                           @Valid @RequestBody RoleUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(roleService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Role deleted", null));
    }
}
