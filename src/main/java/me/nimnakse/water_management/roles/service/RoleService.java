package me.nimnakse.water_management.roles.service;

import java.util.List;
import me.nimnakse.water_management.roles.dto.RoleCreateReq;
import me.nimnakse.water_management.roles.dto.RoleRes;
import me.nimnakse.water_management.roles.dto.RoleUpdateReq;

public interface RoleService {
    List<RoleRes> getRoles();

    RoleRes getRole(Long id);

    RoleRes create(RoleCreateReq request);

    RoleRes update(Long id, RoleUpdateReq request);

    void delete(Long id);
}
