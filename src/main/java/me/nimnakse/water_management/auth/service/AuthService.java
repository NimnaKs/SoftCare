package me.nimnakse.water_management.auth.service;

import io.jsonwebtoken.Claims;
import me.nimnakse.water_management.auth.dto.AuthRes;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.organization.entity.OrgUnit;
import me.nimnakse.water_management.organization.repository.OrganizationRepository;
import me.nimnakse.water_management.roles.entity.RoleAppScope;
import me.nimnakse.water_management.security.CustomUserDetailsService;
import me.nimnakse.water_management.security.JwtService;
import me.nimnakse.water_management.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final OrganizationRepository organizationRepository;
    private final long accessTokenValidityMs;

    public AuthService(AuthenticationManager authenticationManager,
            CustomUserDetailsService userDetailsService,
            JwtService jwtService,
            OrganizationRepository organizationRepository,
            @Value("${security.jwt.access-token-validity-ms}") long accessTokenValidityMs) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.organizationRepository = organizationRepository;
        this.accessTokenValidityMs = accessTokenValidityMs;
    }

    public AuthRes login(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Long organizationId = resolveOrganizationId(principal);
        String accessToken = jwtService.generateAccessToken(principal, organizationId);
        String refreshToken = jwtService.generateRefreshToken(principal);
        return new AuthRes(accessToken, refreshToken, "Bearer", accessTokenValidityMs / 1000);
    }

    public AuthRes refresh(String refreshToken) {
        Claims claims = jwtService.parseClaims(refreshToken);
        if (!jwtService.isRefreshToken(claims)) {
            throw new BadRequestException("Invalid refresh token", "වලංගු නොවන නැවුම් කිරීමේ ටෝකනය");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());
        UserPrincipal principal = (UserPrincipal) userDetails;
        Long organizationId = resolveOrganizationId(principal);
        String accessToken = jwtService.generateAccessToken(principal, organizationId);
        String newRefreshToken = jwtService.generateRefreshToken(principal);
        return new AuthRes(accessToken, newRefreshToken, "Bearer", accessTokenValidityMs / 1000);
    }

    private Long resolveOrganizationId(UserPrincipal principal) {
        if (!principal.getAppScopes().contains(RoleAppScope.BRANCH_APP)) {
            return null;
        }

        OrgUnit orgUnit = principal.getUser().getOrgUnit();
        if (orgUnit == null) {
            throw new BadRequestException("Branch user must be assigned to an org unit",
                    "ශාඛා පරිශීලකයෙකු ආයතන ඒකකයකට අනුයුක්ත කළ යුතුය");
        }

        return orgUnit.getId();
    }
}
