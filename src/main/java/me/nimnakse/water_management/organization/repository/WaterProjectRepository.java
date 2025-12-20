package me.nimnakse.water_management.organization.repository;

import me.nimnakse.water_management.organization.entity.WaterProject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaterProjectRepository extends JpaRepository<WaterProject, Long> {
}
