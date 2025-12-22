package me.nimnakse.water_management.societies.repository;

import me.nimnakse.water_management.societies.entity.Society;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocietyRepository extends JpaRepository<Society, Long> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
