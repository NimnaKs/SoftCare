package me.nimnakse.water_management.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static Long getOrganizationId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object details = authentication.getDetails();
        if (details instanceof JwtAuthenticationDetails jwtDetails) {
            return jwtDetails.getOrganizationId();
        }
        return null;
    }

    public static boolean hasAnyAppScope(me.nimnakse.water_management.roles.entity.RoleAppScope... scopes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        for (me.nimnakse.water_management.roles.entity.RoleAppScope scope : scopes) {
            String authority = "APP_SCOPE_" + scope.name();
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(authority))) {
                return true;
            }
        }
        return false;
    }
}
