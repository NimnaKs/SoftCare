package me.nimnakse.water_management.valves.repository;

import me.nimnakse.water_management.valves.entity.Valve;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValveRepository extends JpaRepository<Valve, Long> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
