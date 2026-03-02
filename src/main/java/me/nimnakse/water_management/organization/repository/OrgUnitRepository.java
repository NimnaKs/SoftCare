package me.nimnakse.water_management.organization.repository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

import me.nimnakse.water_management.organization.entity.OrgUnit;
import me.nimnakse.water_management.organization.entity.OrgUnitLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrgUnitRepository extends JpaRepository<OrgUnit, Long> {
    List<OrgUnit> findByWaterProjectId(Long waterProjectId);

    Page<OrgUnit> findByLevel(OrgUnitLevel level, Pageable pageable);

    Page<OrgUnit> findByLevelAndParentId(OrgUnitLevel level, Long parentId, Pageable pageable);

    boolean existsByOrganizationCode(String organizationCode);

    Optional<OrgUnit> findTopByLevelOrderByOrganizationCodeDesc(OrgUnitLevel level);

    @Query("select max(o.organizationCode) from OrgUnit o where o.level = :level")
    String findMaxOrganizationCodeByLevel(@Param("level") OrgUnitLevel level);

    List<OrgUnit> findByParentIdIn(Collection<Long> parentIds);
}
