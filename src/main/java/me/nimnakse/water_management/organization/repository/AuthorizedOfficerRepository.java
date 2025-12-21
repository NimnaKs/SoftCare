package me.nimnakse.water_management.organization.repository;

import me.nimnakse.water_management.organization.entity.AuthorizedOfficer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuthorizedOfficerRepository extends JpaRepository<AuthorizedOfficer, Long> {
    List<AuthorizedOfficer> findByOrganizationIdAndDeletedAtIsNull(Long organizationId);

    Optional<AuthorizedOfficer> findByIdAndDeletedAtIsNull(Long id);
}
