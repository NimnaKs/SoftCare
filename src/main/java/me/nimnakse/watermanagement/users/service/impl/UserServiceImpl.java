package me.nimnakse.watermanagement.users.service.impl;

import java.util.List;
import me.nimnakse.watermanagement.common.exception.BadRequestException;
import me.nimnakse.watermanagement.common.exception.ErrorCode;
import me.nimnakse.watermanagement.common.exception.NotFoundException;
import me.nimnakse.watermanagement.roles.dto.RoleRes;
import me.nimnakse.watermanagement.roles.entity.Role;
import me.nimnakse.watermanagement.roles.entity.RoleAppScope;
import me.nimnakse.watermanagement.roles.repository.RoleRepository;
import me.nimnakse.watermanagement.users.dto.request.UserCreateReq;
import me.nimnakse.watermanagement.users.dto.request.UserUpdateReq;
import me.nimnakse.watermanagement.users.dto.response.UserRes;
import me.nimnakse.watermanagement.users.entity.User;
import me.nimnakse.watermanagement.users.entity.UserRole;
import me.nimnakse.watermanagement.users.entity.UserStatus;
import me.nimnakse.watermanagement.users.repository.UserRepository;
import me.nimnakse.watermanagement.users.repository.UserRoleRepository;
import me.nimnakse.watermanagement.users.service.UserMapper;
import me.nimnakse.watermanagement.users.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository,
                           UserRoleRepository userRoleRepository,
                           RoleRepository roleRepository,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
    }

    @Transactional
    @Override
    public UserRes create(UserCreateReq request) {
        User user = new User();
        user.setUsername(request.nic());
        user.setPasswordHash(request.passwordHash());
        user.setNic(request.nic());
        user.setName(request.name());
        user.setMobileNumber(request.mobileNumber());
        user.setSecondaryContactNumber(request.secondaryContactNumber());
        user.setAddress(request.address());
        user.setProfilePhotoUrl(request.profilePhotoUrl());
        user.setOrgUnitId(request.orgUnitId());
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        List<Role> roles = loadAndValidateRoles(request.roleIds(), request.appScope());
        saveUserRoles(savedUser, roles);

        return buildUserResponse(savedUser, roles);
    }

    @Transactional
    @Override
    public UserRes update(Long id, UserUpdateReq request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found", ErrorCode.USER_NOT_FOUND));

        user.setNic(request.nic());
        user.setUsername(request.nic());
        user.setName(request.name());
        user.setMobileNumber(request.mobileNumber());
        user.setSecondaryContactNumber(request.secondaryContactNumber());
        user.setAddress(request.address());
        user.setProfilePhotoUrl(request.profilePhotoUrl());
        user.setOrgUnitId(request.orgUnitId());

        List<Role> roles = loadAndValidateRoles(request.roleIds(), request.appScope());
        userRoleRepository.deleteByIdUserId(user.getId());
        saveUserRoles(user, roles);

        return buildUserResponse(user, roles);
    }

    @Transactional(readOnly = true)
    @Override
    public UserRes getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found", ErrorCode.USER_NOT_FOUND));
        List<RoleRes> roles = userRoleRepository.findByIdUserId(user.getId()).stream()
                .map(UserRole::getRole)
                .map(role -> new RoleRes(role.getId(), role.getName(), role.getDescription(), role.getAppScope()))
                .toList();
        UserRes baseRes = userMapper.toUserRes(user);
        return new UserRes(baseRes.id(), baseRes.username(), baseRes.nic(), baseRes.name(), baseRes.mobileNumber(),
                baseRes.secondaryContactNumber(), baseRes.address(), baseRes.profilePhotoUrl(), baseRes.status(),
                baseRes.orgUnitId(), roles);
    }

    @Transactional
    @Override
    public void deactivate(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found", ErrorCode.USER_NOT_FOUND));
        user.setStatus(UserStatus.DEACTIVATED);
    }

    private List<Role> loadAndValidateRoles(List<Long> roleIds, RoleAppScope appScope) {
        List<Role> roles = roleRepository.findByIdIn(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new NotFoundException("One or more roles not found", ErrorCode.ROLE_NOT_FOUND);
        }
        boolean matchesScope = roles.stream().allMatch(role -> role.getAppScope() == appScope);
        if (!matchesScope) {
            throw new BadRequestException("Roles do not match requested app scope");
        }
        return roles;
    }

    private void saveUserRoles(User user, List<Role> roles) {
        List<UserRole> mappings = roles.stream()
                .map(role -> new UserRole(user, role))
                .toList();
        userRoleRepository.saveAll(mappings);
    }

    private UserRes buildUserResponse(User user, List<Role> roles) {
        List<RoleRes> roleRes = roles.stream()
                .map(role -> new RoleRes(role.getId(), role.getName(), role.getDescription(), role.getAppScope()))
                .toList();
        UserRes baseRes = userMapper.toUserRes(user);
        return new UserRes(baseRes.id(), baseRes.username(), baseRes.nic(), baseRes.name(), baseRes.mobileNumber(),
                baseRes.secondaryContactNumber(), baseRes.address(), baseRes.profilePhotoUrl(), baseRes.status(),
                baseRes.orgUnitId(), roleRes);
    }
}
