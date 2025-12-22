package me.nimnakse.water_management.gn_divisions.repository;

import me.nimnakse.water_management.gn_divisions.entity.GnDivision;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GnDivisionRepository extends JpaRepository<GnDivision, Long> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
