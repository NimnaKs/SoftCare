package me.nimnakse.water_management.tariffs.repository;

import me.nimnakse.water_management.tariffs.entity.Tariff;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TariffRepository extends JpaRepository<Tariff, Long> {
    java.util.List<Tariff> findByOrgUnitId(Long orgUnitId);

    boolean existsByOrgUnitIdAndNameIgnoreCase(Long orgUnitId, String name);

    boolean existsByOrgUnitIdAndNameIgnoreCaseAndIdNot(Long orgUnitId, String name, Long id);
}
