package me.nimnakse.water_management.organization.repository;

import me.nimnakse.water_management.organization.entity.AuthorizedOfficer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorizedOfficerRepository extends JpaRepository<AuthorizedOfficer, Long> {
    List<AuthorizedOfficer> findByOrganizationId(Long organizationId);
}
