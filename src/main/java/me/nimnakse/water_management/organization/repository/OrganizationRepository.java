package me.nimnakse.water_management.organization.repository;

import java.util.Optional;
import me.nimnakse.water_management.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByOrgUnitId(Long orgUnitId);

    @Query(value = "SELECT MAX(CAST(organization_code AS UNSIGNED)) FROM organizations", nativeQuery = true)
    Long findMaxOrganizationCode();
}
