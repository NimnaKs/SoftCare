package me.nimnakse.water_management.users.seed;

import me.nimnakse.water_management.roles.entity.Role;
import me.nimnakse.water_management.roles.entity.RoleAppScope;
import me.nimnakse.water_management.roles.repository.RoleRepository;
import me.nimnakse.water_management.users.entity.User;
import me.nimnakse.water_management.users.entity.UserRole;
import me.nimnakse.water_management.users.entity.UserRoleId;
import me.nimnakse.water_management.users.entity.UserStatus;
import me.nimnakse.water_management.users.repository.UserRepository;
import me.nimnakse.water_management.users.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SuperAdminSeeder implements CommandLineRunner {
    private static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    private final String username;
    private final String password;
    private final String nic;
    private final String name;
    private final String mobileNumber;

    public SuperAdminSeeder(UserRepository userRepository,
                            RoleRepository roleRepository,
                            UserRoleRepository userRoleRepository,
                            PasswordEncoder passwordEncoder,
                            @Value("${app.seed.super-admin.username:superadmin}") String username,
                            @Value("${app.seed.super-admin.password:ChangeMe123!}") String password,
                            @Value("${app.seed.super-admin.nic:SA000000}") String nic,
                            @Value("${app.seed.super-admin.name:Super Admin}") String name,
                            @Value("${app.seed.super-admin.mobile-number:+10000000000}") String mobileNumber) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
        this.nic = nic;
        this.name = name;
        this.mobileNumber = mobileNumber;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role role = roleRepository.findByName(SUPER_ADMIN_ROLE)
                .orElseGet(this::createSuperAdminRole);

        User user = userRepository.findByUsername(username)
                .orElseGet(() -> createSuperAdminUser(username, password, nic, name, mobileNumber));

        UserRoleId mappingId = new UserRoleId(user.getId(), role.getId());
        if (userRoleRepository.findById(mappingId).isEmpty()) {
            userRoleRepository.save(new UserRole(user, role));
        }
    }

    private Role createSuperAdminRole() {
        Role role = new Role();
        role.setName(SUPER_ADMIN_ROLE);
        role.setDescription("Super administrator");
        role.setAppScope(RoleAppScope.ADMIN_PORTAL);
        return roleRepository.save(role);
    }

    private User createSuperAdminUser(String username,
                                      String password,
                                      String nic,
                                      String name,
                                      String mobileNumber) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setNic(nic);
        user.setName(name);
        user.setMobileNumber(mobileNumber);
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }
}
