package me.nimnakse.water_management.societies.repository;

import me.nimnakse.water_management.societies.entity.Society;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocietyRepository extends JpaRepository<Society, Long> {
    java.util.List<Society> findByOrgUnitId(Long orgUnitId);

    boolean existsByOrgUnitIdAndNameIgnoreCase(Long orgUnitId, String name);

    boolean existsByOrgUnitIdAndNameIgnoreCaseAndIdNot(Long orgUnitId, String name, Long id);
}
