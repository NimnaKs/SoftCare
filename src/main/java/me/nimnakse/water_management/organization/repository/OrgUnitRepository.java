package me.nimnakse.water_management.organization.repository;

import java.util.List;
import me.nimnakse.water_management.organization.entity.OrgUnit;
import me.nimnakse.water_management.organization.entity.OrgUnitLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrgUnitRepository extends JpaRepository<OrgUnit, Long> {
    List<OrgUnit> findByWaterProjectId(Long waterProjectId);

    Page<OrgUnit> findByLevel(OrgUnitLevel level, Pageable pageable);

    Page<OrgUnit> findByLevelAndParentId(OrgUnitLevel level, Long parentId, Pageable pageable);
}
