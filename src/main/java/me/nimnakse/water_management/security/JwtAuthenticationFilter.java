package me.nimnakse.water_management.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import me.nimnakse.water_management.roles.entity.RoleAppScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        log.debug("JWT filter invoked for path: {}", path);

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            log.debug("No Authorization header or not Bearer token");
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();
        log.debug("Bearer token found");

        try {
            Claims claims = jwtService.parseClaims(token);
            String username = claims.getSubject();

            log.debug("JWT parsed successfully, subject={}", username);

            if (username == null || username.isBlank()) {
                log.warn("JWT subject is missing or blank");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                log.debug("SecurityContext already authenticated, skipping");
                filterChain.doFilter(request, response);
                return;
            }

            Set<String> tokenScopes = extractTokenScopes(claims);
            log.debug("Token scopes: {}", tokenScopes);

            var userDetails = userDetailsService.loadUserByUsername(username);
            log.debug("User loaded from DB: {}", userDetails.getUsername());

            if (!(userDetails instanceof UserPrincipal principal)) {
                log.warn("UserDetails is not instance of UserPrincipal");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            Set<String> dbScopes = principal.getAppScopes().stream()
                    .map(RoleAppScope::name)
                    .collect(java.util.stream.Collectors.toSet());

            log.debug("DB scopes: {}", dbScopes);

            if (!dbScopes.containsAll(tokenScopes)) {
                log.warn("Scope mismatch! tokenScopes={} dbScopes={}", tokenScopes, dbScopes);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            List<GrantedAuthority> authorities = dbScopes.stream()
                    .map(scope -> (GrantedAuthority) new SimpleGrantedAuthority("APP_SCOPE_" + scope))
                    .toList();

            log.debug("Granted authorities: {}", authorities);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("Authentication SUCCESS for user={} scopes={}", username, dbScopes);

            filterChain.doFilter(request, response);

        } catch (JwtException ex) {
            log.warn("JWT validation failed: {}", ex.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private Set<String> extractTokenScopes(Claims claims) {
        Object scopesObj = claims.get("scopes");

        if (scopesObj == null) {
            log.debug("No scopes claim found in token");
            return Set.of();
        }

        if (scopesObj instanceof List<?> rawList) {
            Set<String> scopes = new HashSet<>();
            for (Object item : rawList) {
                String scope = Objects.toString(item, "").trim();
                if (!scope.isEmpty()) scopes.add(scope);
            }
            return Set.copyOf(scopes);
        }

        if (scopesObj instanceof String s) {
            String scope = s.trim();
            return scope.isEmpty() ? Set.of() : Set.of(scope);
        }

        log.debug("Scopes claim has unexpected type: {}", scopesObj.getClass().getName());
        return Set.of();
    }
}
