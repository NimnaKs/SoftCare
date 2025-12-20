package me.nimnakse.water_management.security;

import java.util.Collection;
import java.util.List;
import me.nimnakse.water_management.roles.entity.Role;
import me.nimnakse.water_management.roles.entity.RoleAppScope;
import me.nimnakse.water_management.users.entity.User;
import me.nimnakse.water_management.users.entity.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {
    private final User user;
    private final List<Role> roles;
    private final List<GrantedAuthority> authorities;

    public UserPrincipal(User user, List<Role> roles) {
        this.user = user;
        this.roles = roles;
        this.authorities = buildAuthorities(roles);
    }

    private List<GrantedAuthority> buildAuthorities(List<Role> roles) {
        List<GrantedAuthority> granted = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()))
                .distinct()
                .map(GrantedAuthority.class::cast)
                .toList();
        List<GrantedAuthority> scopes = roles.stream()
                .map(Role::getAppScope)
                .map(scope -> new SimpleGrantedAuthority("APP_SCOPE_" + scope.name()))
                .distinct()
                .map(GrantedAuthority.class::cast)
                .toList();
        java.util.ArrayList<GrantedAuthority> combined = new java.util.ArrayList<>();
        combined.addAll(granted);
        combined.addAll(scopes);
        return List.copyOf(combined);
    }

    public User getUser() {
        return user;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public List<RoleAppScope> getAppScopes() {
        return roles.stream()
                .map(Role::getAppScope)
                .distinct()
                .toList();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == UserStatus.ACTIVE;
    }
}
