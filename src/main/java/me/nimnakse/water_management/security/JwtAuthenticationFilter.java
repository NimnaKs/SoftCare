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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

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

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();

        try {
            Claims claims = jwtService.parseClaims(token);
            String username = claims.getSubject();

            if (username == null || username.isBlank()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            Set<String> tokenScopes = extractTokenScopes(claims);

            var userDetails = userDetailsService.loadUserByUsername(username);
            if (!(userDetails instanceof UserPrincipal principal)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            Set<String> dbScopes = principal.getAppScopes().stream()
                    .map(RoleAppScope::name)
                    .collect(java.util.stream.Collectors.toSet());

            if (!dbScopes.containsAll(tokenScopes)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            List<GrantedAuthority> authorities = dbScopes.stream()
                    .map(scope -> (GrantedAuthority) new SimpleGrantedAuthority("APP_SCOPE_" + scope))
                    .toList();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (JwtException ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private Set<String> extractTokenScopes(Claims claims) {
        Object scopesObj = claims.get("scopes");
        if (scopesObj == null) return Set.of();

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

        return Set.of();
    }
}
