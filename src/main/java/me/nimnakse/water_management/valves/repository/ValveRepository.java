package me.nimnakse.water_management.valves.repository;

import me.nimnakse.water_management.valves.entity.Valve;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValveRepository extends JpaRepository<Valve, Long> {
    java.util.List<Valve> findByOrgUnitId(Long orgUnitId);

    boolean existsByOrgUnitIdAndNameIgnoreCase(Long orgUnitId, String name);

    boolean existsByOrgUnitIdAndNameIgnoreCaseAndIdNot(Long orgUnitId, String name, Long id);
}
