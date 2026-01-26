package me.nimnakse.water_management.gn_divisions.repository;

import me.nimnakse.water_management.gn_divisions.entity.GnDivision;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GnDivisionRepository extends JpaRepository<GnDivision, Long> {
    java.util.List<GnDivision> findByOrgUnitId(Long orgUnitId);

    boolean existsByOrgUnitIdAndNameIgnoreCase(Long orgUnitId, String name);

    boolean existsByOrgUnitIdAndNameIgnoreCaseAndIdNot(Long orgUnitId, String name, Long id);
}
