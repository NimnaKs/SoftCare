package me.nimnakse.water_management.roles.service;

import java.time.Instant;
import java.util.List;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.roles.dto.RoleCreateReq;
import me.nimnakse.water_management.roles.dto.RoleRes;
import me.nimnakse.water_management.roles.dto.RoleUpdateReq;
import me.nimnakse.water_management.roles.entity.Role;
import me.nimnakse.water_management.roles.repository.RoleRepository;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<RoleRes> getRoles() {
        return roleRepository.findAllByDeletedAtIsNull().stream()
                .map(this::toRes)
                .toList();
    }

    @Override
    public RoleRes getRole(Long id) {
        Role role = roleRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Role not found", ErrorCode.ROLE_NOT_FOUND));
        return toRes(role);
    }

    @Override
    public RoleRes create(RoleCreateReq request) {
        Role role = new Role();
        role.setName(request.name());
        role.setDescription(request.description());
        role.setAppScope(request.appScope());
        return toRes(roleRepository.save(role));
    }

    @Override
    public RoleRes update(Long id, RoleUpdateReq request) {
        Role role = roleRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Role not found", ErrorCode.ROLE_NOT_FOUND));
        role.setName(request.name());
        role.setDescription(request.description());
        role.setAppScope(request.appScope());
        Role saved = roleRepository.save(role);
        return toRes(saved);
    }

    @Override
    public void delete(Long id) {
        Role role = roleRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Role not found", ErrorCode.ROLE_NOT_FOUND));
        role.setDeletedAt(Instant.now());
        role.setName(role.getName() + "_deleted_" + id);
        roleRepository.save(role);
    }

    private RoleRes toRes(Role role) {
        return new RoleRes(role.getId(), role.getName(), role.getDescription(), role.getAppScope());
    }
}
