package me.nimnakse.water_management.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class JwtAuthenticationDetails extends WebAuthenticationDetails {
    private final Long organizationId;
    private final Long agencyId;

    public JwtAuthenticationDetails(HttpServletRequest request, Long organizationId, Long agencyId) {
        super(request);
        this.organizationId = organizationId;
        this.agencyId = agencyId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public Long getAgencyId() {
        return agencyId;
    }
}
