package me.nimnakse.water_management.security;

import java.util.List;
import me.nimnakse.water_management.roles.entity.Role;
import me.nimnakse.water_management.users.entity.User;
import me.nimnakse.water_management.users.entity.UserRole;
import me.nimnakse.water_management.users.repository.UserRepository;
import me.nimnakse.water_management.users.repository.UserRoleRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public CustomUserDetailsService(UserRepository userRepository, UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<Role> roles = userRoleRepository.findByIdUserId(user.getId()).stream()
                .map(UserRole::getRole)
                .toList();
        return new UserPrincipal(user, roles);
    }
}
