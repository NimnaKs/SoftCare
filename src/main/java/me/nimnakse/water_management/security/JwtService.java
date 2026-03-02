package me.nimnakse.water_management.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final Key signingKey;
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;

    public JwtService(@Value("${security.jwt.secret}") String secret,
                      @Value("${security.jwt.access-token-validity-ms}") long accessTokenValidityMs,
                      @Value("${security.jwt.refresh-token-validity-ms}") long refreshTokenValidityMs) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityMs = accessTokenValidityMs;
        this.refreshTokenValidityMs = refreshTokenValidityMs;
    }

    public String generateAccessToken(UserPrincipal principal, Long organizationId, Long agencyId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidityMs);
        List<String> roles = principal.getRoles().stream()
                .map(role -> role.getName())
                .toList();
        List<String> scopes = principal.getAppScopes().stream()
                .map(Enum::name)
                .toList();
        var builder = Jwts.builder()
                .setSubject(principal.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim("roles", roles)
                .claim("scopes", scopes);
        if (organizationId != null) {
            builder.claim("organizationId", organizationId);
        }
        if (agencyId != null) {
            builder.claim("agencyId", agencyId);
        }
        return builder.signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UserPrincipal principal) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenValidityMs);
        return Jwts.builder()
                .setSubject(principal.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim("type", "refresh")
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isRefreshToken(Claims claims) {
        return "refresh".equals(claims.get("type", String.class));
    }
}
