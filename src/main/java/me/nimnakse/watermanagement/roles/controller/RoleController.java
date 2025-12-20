package me.nimnakse.watermanagement.roles.controller;

import java.util.List;
import me.nimnakse.watermanagement.common.api.ApiResponse;
import me.nimnakse.watermanagement.roles.dto.RoleRes;
import me.nimnakse.watermanagement.roles.service.RoleService;
import org.springframework.web.bind.annotation.GetMapping;
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
}
