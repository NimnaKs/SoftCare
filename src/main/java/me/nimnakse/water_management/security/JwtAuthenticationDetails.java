package me.nimnakse.water_management.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class JwtAuthenticationDetails extends WebAuthenticationDetails {
    private final Long organizationId;

    public JwtAuthenticationDetails(HttpServletRequest request, Long organizationId) {
        super(request);
        this.organizationId = organizationId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }
}
