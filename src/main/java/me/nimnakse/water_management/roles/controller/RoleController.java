package me.nimnakse.water_management.roles.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.roles.dto.RoleCreateReq;
import me.nimnakse.water_management.roles.dto.RoleRes;
import me.nimnakse.water_management.roles.dto.RoleUpdateReq;
import me.nimnakse.water_management.roles.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roles")
@Tag(name = "Roles", description = "Role management operations")
@CrossOrigin
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @Operation(summary = "List roles", description = "Returns all configured roles.")
    public ApiResponse<List<RoleRes>> listRoles() {
        return ApiResponse.success(roleService.getRoles());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get role", description = "Fetches a role by identifier.")
    public ResponseEntity<ApiResponse<RoleRes>> getRole(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(roleService.getRole(id)));
    }

    @PostMapping
    @Operation(summary = "Create role", description = "Creates a new role.")
    public ResponseEntity<ApiResponse<RoleRes>> createRole(@Valid @RequestBody RoleCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(roleService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update role", description = "Updates role details.")
    public ResponseEntity<ApiResponse<RoleRes>> updateRole(@PathVariable Long id,
                                                           @Valid @RequestBody RoleUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(roleService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete role", description = "Deletes a role by identifier.")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Role deleted", null));
    }
}
