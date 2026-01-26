package me.nimnakse.water_management.clusters.repository;

import me.nimnakse.water_management.clusters.entity.Cluster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClusterRepository extends JpaRepository<Cluster, Long> {
    java.util.List<Cluster> findByOrgUnitId(Long orgUnitId);

    boolean existsByOrgUnitIdAndNameIgnoreCase(Long orgUnitId, String name);

    boolean existsByOrgUnitIdAndNameIgnoreCaseAndIdNot(Long orgUnitId, String name, Long id);
}
