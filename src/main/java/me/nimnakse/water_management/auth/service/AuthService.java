package me.nimnakse.water_management.auth.service;

import io.jsonwebtoken.Claims;
import me.nimnakse.water_management.auth.dto.AuthRes;
import me.nimnakse.water_management.common.exception.BadRequestException;
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
    private final long accessTokenValidityMs;

    public AuthService(AuthenticationManager authenticationManager,
                       CustomUserDetailsService userDetailsService,
                       JwtService jwtService,
                       @Value("${security.jwt.access-token-validity-ms}") long accessTokenValidityMs) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.accessTokenValidityMs = accessTokenValidityMs;
    }

    public AuthRes login(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = jwtService.generateRefreshToken(principal);
        return new AuthRes(accessToken, refreshToken, "Bearer", accessTokenValidityMs / 1000);
    }

    public AuthRes refresh(String refreshToken) {
        Claims claims = jwtService.parseClaims(refreshToken);
        if (!jwtService.isRefreshToken(claims)) {
            throw new BadRequestException("Invalid refresh token");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());
        UserPrincipal principal = (UserPrincipal) userDetails;
        String accessToken = jwtService.generateAccessToken(principal);
        String newRefreshToken = jwtService.generateRefreshToken(principal);
        return new AuthRes(accessToken, newRefreshToken, "Bearer", accessTokenValidityMs / 1000);
    }
}
