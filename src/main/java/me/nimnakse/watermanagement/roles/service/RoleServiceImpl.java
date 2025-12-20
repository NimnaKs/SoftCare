package me.nimnakse.watermanagement.roles.service;

import java.util.List;
import me.nimnakse.watermanagement.roles.dto.RoleRes;
import me.nimnakse.watermanagement.roles.entity.Role;
import me.nimnakse.watermanagement.roles.repository.RoleRepository;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<RoleRes> getRoles() {
        return roleRepository.findAll().stream()
                .map(this::toRes)
                .toList();
    }

    private RoleRes toRes(Role role) {
        return new RoleRes(role.getId(), role.getName(), role.getDescription(), role.getAppScope());
    }
}
