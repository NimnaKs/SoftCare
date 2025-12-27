package me.nimnakse.water_management.users.service.impl;

import java.util.List;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.roles.dto.RoleRes;
import me.nimnakse.water_management.roles.entity.Role;
import me.nimnakse.water_management.roles.entity.RoleAppScope;
import me.nimnakse.water_management.roles.repository.RoleRepository;
import me.nimnakse.water_management.organization.entity.OrgUnit;
import me.nimnakse.water_management.organization.repository.OrgUnitRepository;
import me.nimnakse.water_management.users.dto.request.UserCreateReq;
import me.nimnakse.water_management.users.dto.request.UserPasswordUpdateReq;
import me.nimnakse.water_management.users.dto.request.UserUpdateReq;
import me.nimnakse.water_management.users.dto.response.UserRes;
import me.nimnakse.water_management.users.entity.User;
import me.nimnakse.water_management.users.entity.UserRole;
import me.nimnakse.water_management.users.entity.UserStatus;
import me.nimnakse.water_management.users.repository.UserRepository;
import me.nimnakse.water_management.users.repository.UserRoleRepository;
import me.nimnakse.water_management.users.service.UserMapper;
import me.nimnakse.water_management.users.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final OrgUnitRepository orgUnitRepository;

    public UserServiceImpl(UserRepository userRepository,
                           UserRoleRepository userRoleRepository,
                           RoleRepository roleRepository,
                           UserMapper userMapper,
                           PasswordEncoder passwordEncoder,
                           OrgUnitRepository orgUnitRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.orgUnitRepository = orgUnitRepository;
    }

    @Transactional
    @Override
    public UserRes create(UserCreateReq request) {
        OrgUnit orgUnit = resolveOrgUnit(request.orgUnitId());
        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.passwordHash()));
        user.setNic(request.nic());
        user.setName(request.name());
        user.setMobileNumber(request.mobileNumber());
        user.setSecondaryContactNumber(request.secondaryContactNumber());
        user.setAddress(request.address());
        user.setProfilePhotoUrl(request.profilePhotoUrl());
        user.setOrgUnit(orgUnit);
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

        OrgUnit orgUnit = resolveOrgUnit(request.orgUnitId());
        user.setNic(request.nic());
        user.setUsername(request.username());
        user.setName(request.name());
        user.setMobileNumber(request.mobileNumber());
        user.setSecondaryContactNumber(request.secondaryContactNumber());
        user.setAddress(request.address());
        user.setProfilePhotoUrl(request.profilePhotoUrl());
        user.setOrgUnit(orgUnit);

        user = userRepository.save(user);

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
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void activate(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found", ErrorCode.USER_NOT_FOUND));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void updatePassword(Long id, UserPasswordUpdateReq request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found", ErrorCode.USER_NOT_FOUND));
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserRes> list(Long orgUnitId, UserStatus status) {

        List<User> users;

        if (orgUnitId != null && status != null) {
            users = userRepository.findAllByOrgUnit_IdAndStatus(orgUnitId, status);
        } else if (orgUnitId != null) {
            users = userRepository.findAllByOrgUnit_Id(orgUnitId);
        } else if (status != null) {
            users = userRepository.findAllByStatus(status);
        } else {
            users = userRepository.findAll();
        }

        // attach roles for each user
        return users.stream().map(u -> {
            List<RoleRes> roles = userRoleRepository.findByIdUserId(u.getId()).stream()
                    .map(UserRole::getRole)
                    .map(r -> new RoleRes(r.getId(), r.getName(), r.getDescription(), r.getAppScope()))
                    .toList();

            UserRes base = userMapper.toUserRes(u);

            return new UserRes(
                    base.id(), base.username(), base.nic(), base.name(), base.mobileNumber(),
                    base.secondaryContactNumber(), base.address(), base.profilePhotoUrl(),
                    base.status(), base.orgUnitId(), roles
            );
        }).toList();
    }


    private List<Role> loadAndValidateRoles(List<Long> roleIds, RoleAppScope appScope) {
        List<Role> roles = roleRepository.findByIdInAndDeletedAtIsNull(roleIds);
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

    private OrgUnit resolveOrgUnit(Long orgUnitId) {
        if (orgUnitId == null) {
            return null;
        }
        return orgUnitRepository.findById(orgUnitId)
                .orElseThrow(() -> new NotFoundException("Org unit not found", ErrorCode.NOT_FOUND));
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
