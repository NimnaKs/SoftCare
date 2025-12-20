package me.nimnakse.water_management.organization.repository;

import java.util.List;
import me.nimnakse.water_management.organization.entity.OrgUnit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrgUnitRepository extends JpaRepository<OrgUnit, Long> {
    List<OrgUnit> findByWaterProjectId(Long waterProjectId);
}
