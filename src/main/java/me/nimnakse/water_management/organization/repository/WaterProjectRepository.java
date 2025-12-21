package me.nimnakse.water_management.organization.repository;

import me.nimnakse.water_management.organization.entity.WaterProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WaterProjectRepository extends JpaRepository<WaterProject, Long> {
    Optional<WaterProject> findByIdAndDeletedAtIsNull(Long id);

    List<WaterProject> findAllByDeletedAtIsNull();
}
