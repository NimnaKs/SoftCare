package me.nimnakse.water_management.organization.repository;

import java.util.List;
import java.util.Optional;
import me.nimnakse.water_management.organization.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByOrgUnitIdAndDeletedAtIsNull(Long orgUnitId);

    Optional<Organization> findByIdAndDeletedAtIsNull(Long id);

    List<Organization> findAllByDeletedAtIsNull();

    Page<Organization> findAllByDeletedAtIsNull(Pageable pageable);

    boolean existsByIdAndDeletedAtIsNull(Long id);

    @Query(value = "SELECT MAX(CAST(organization_code AS UNSIGNED)) FROM organizations", nativeQuery = true)
    Long findMaxOrganizationCode();
}
