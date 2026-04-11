package me.nimnakse.water_management.users.service.impl;

import java.util.List;
import me.nimnakse.water_management.common.api.PageResponse;
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
import me.nimnakse.water_management.sms.service.SmsNotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final OrgUnitRepository orgUnitRepository;
    private final me.nimnakse.water_management.agencies.repository.AgencyRepository agencyRepository;
    private final SmsNotificationService smsNotificationService;
    private static final SecureRandom RANDOM = new SecureRandom();

    public UserServiceImpl(UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            RoleRepository roleRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            OrgUnitRepository orgUnitRepository,
            me.nimnakse.water_management.agencies.repository.AgencyRepository agencyRepository,
            SmsNotificationService smsNotificationService) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.orgUnitRepository = orgUnitRepository;
        this.agencyRepository = agencyRepository;
        this.smsNotificationService = smsNotificationService;
    }

    @Transactional
    @Override
    public UserRes create(UserCreateReq request) {
        validateAgencySelection(request.appScope(), request.agencyId());
        OrgUnit orgUnit = resolveOrgUnit(request.orgUnitId());
        me.nimnakse.water_management.agencies.entity.Agency agency = resolveAgency(request.agencyId());
        String rawPassword = hasText(request.passwordHash()) ? request.passwordHash() : generateTemporaryPassword();

        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setNic(request.nic());
        user.setName(request.name());
        user.setMobileNumber(request.mobileNumber());
        user.setSecondaryContactNumber(request.secondaryContactNumber());
        user.setAddress(request.address());
        user.setProfilePhotoUrl(request.profilePhotoUrl());
        user.setOrgUnit(orgUnit);
        user.setAgency(agency);
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        List<Role> roles = loadAndValidateRoles(request.roleIds(), request.appScope());
        saveUserRoles(savedUser, roles);

        try {
            smsNotificationService.sendWelcomeMessage(savedUser.getMobileNumber(), savedUser.getName());
        } catch (Exception e) {
            System.err.println("Failed to send welcome SMS for user " + savedUser.getUsername() + ": " + e.getMessage());
        }

        return buildUserResponse(savedUser, roles);
    }

    @Transactional
    @Override
    public UserRes update(Long id, UserUpdateReq request) {
        validateAgencySelection(request.appScope(), request.agencyId());
        User user = userRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException("User not found", "පරිශීලකයා හමු නොවීය", ErrorCode.USER_NOT_FOUND));

        OrgUnit orgUnit = resolveOrgUnit(request.orgUnitId());
        me.nimnakse.water_management.agencies.entity.Agency agency = resolveAgency(request.agencyId());

        user.setNic(request.nic());
        user.setUsername(request.username());
        user.setName(request.name());
        user.setMobileNumber(request.mobileNumber());
        user.setSecondaryContactNumber(request.secondaryContactNumber());
        user.setAddress(request.address());
        user.setProfilePhotoUrl(request.profilePhotoUrl());
        user.setOrgUnit(orgUnit);
        user.setAgency(agency);

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
                .orElseThrow(
                        () -> new NotFoundException("User not found", "පරිශීලකයා හමු නොවීය", ErrorCode.USER_NOT_FOUND));
        List<RoleRes> roles = userRoleRepository.findByIdUserId(user.getId()).stream()
                .map(UserRole::getRole)
                .map(role -> new RoleRes(role.getId(), role.getName(), role.getDescription(), role.getAppScope()))
                .toList();
        UserRes baseRes = userMapper.toUserRes(user);
        return new UserRes(baseRes.id(), baseRes.username(), baseRes.nic(), baseRes.name(), baseRes.mobileNumber(),
                baseRes.secondaryContactNumber(), baseRes.address(), baseRes.profilePhotoUrl(), baseRes.status(),
                baseRes.orgUnitId(), baseRes.agencyId(), roles);
    }

    @Transactional
    @Override
    public void deactivate(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException("User not found", "පරිශීලකයා හමු නොවීය", ErrorCode.USER_NOT_FOUND));
        user.setStatus(UserStatus.DEACTIVATED);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void activate(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException("User not found", "පරිශීලකයා හමු නොවීය", ErrorCode.USER_NOT_FOUND));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void updatePassword(Long id, UserPasswordUpdateReq request) {
        User user = userRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException("User not found", "පරිශීලකයා හමු නොවීය", ErrorCode.USER_NOT_FOUND));
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
            return toUserResWithRoles(u);
        }).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<UserRes> listPaged(Long orgUnitId, UserStatus status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));

        Page<User> usersPage;

        if (orgUnitId != null && status != null) {
            usersPage = userRepository.findByOrgUnit_IdAndStatus(orgUnitId, status, pageRequest);
        } else if (orgUnitId != null) {
            usersPage = userRepository.findByOrgUnit_Id(orgUnitId, pageRequest);
        } else if (status != null) {
            usersPage = userRepository.findByStatus(status, pageRequest);
        } else {
            usersPage = userRepository.findAll(pageRequest);
        }

        List<UserRes> items = usersPage.getContent().stream()
                .map(this::toUserResWithRoles)
                .toList();

        return new PageResponse<>(items, usersPage.getTotalElements(), usersPage.getTotalPages(),
                usersPage.getNumber(), usersPage.getSize());
    }

    private List<Role> loadAndValidateRoles(List<Long> roleIds, RoleAppScope appScope) {
        List<Role> roles = roleRepository.findByIdInAndDeletedAtIsNull(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new NotFoundException("One or more roles not found", "භූමිකාවන් එකක් හෝ කිහිපයක් හමු නොවීය",
                    ErrorCode.ROLE_NOT_FOUND);
        }
        boolean matchesScope = roles.stream().allMatch(role -> role.getAppScope() == appScope);
        if (!matchesScope) {
            throw new BadRequestException("Roles do not match requested app scope",
                    "භූමිකාවන් ඉල්ලන ලද යෙදුම් විෂය පථයට නොගැලපේ");
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
                .orElseThrow(
                        () -> new NotFoundException("Org unit not found", "ආයතන ඒකකය හමු නොවීය", ErrorCode.NOT_FOUND));
    }

    private me.nimnakse.water_management.agencies.entity.Agency resolveAgency(Long agencyId) {
        if (agencyId == null) {
            return null;
        }
        return agencyRepository.findById(agencyId)
                .orElseThrow(
                        () -> new NotFoundException("Agency not found", "නියෝජිතායතනය හමු නොවීය", ErrorCode.NOT_FOUND));
    }

    private void validateAgencySelection(RoleAppScope appScope, Long agencyId) {
        if (appScope == RoleAppScope.AGENCY_APP && agencyId == null) {
            throw new BadRequestException("Agency is required for AGENCY_APP scope",
                    "AGENCY_APP විෂය පථය සඳහා නියෝජිතායතනය අවශ්‍ය වේ");
        }
    }

    private UserRes buildUserResponse(User user, List<Role> roles) {
        List<RoleRes> roleRes = roles.stream()
                .map(role -> new RoleRes(role.getId(), role.getName(), role.getDescription(), role.getAppScope()))
                .toList();
        UserRes baseRes = userMapper.toUserRes(user);
        return new UserRes(baseRes.id(), baseRes.username(), baseRes.nic(), baseRes.name(), baseRes.mobileNumber(),
                baseRes.secondaryContactNumber(), baseRes.address(), baseRes.profilePhotoUrl(), baseRes.status(),
                baseRes.orgUnitId(), baseRes.agencyId(), roleRes);
    }

    private UserRes toUserResWithRoles(User user) {
        List<RoleRes> roles = userRoleRepository.findByIdUserId(user.getId()).stream()
                .map(UserRole::getRole)
                .map(r -> new RoleRes(r.getId(), r.getName(), r.getDescription(), r.getAppScope()))
                .toList();

        UserRes base = userMapper.toUserRes(user);

        return new UserRes(
                base.id(), base.username(), base.nic(), base.name(), base.mobileNumber(),
                base.secondaryContactNumber(), base.address(), base.profilePhotoUrl(),
                base.status(), base.orgUnitId(), base.agencyId(), roles);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String generateTemporaryPassword() {
        final String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            password.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        }
        return password.toString();
    }
}
