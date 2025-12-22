package me.nimnakse.water_management.billing_zones.repository;

import java.util.List;
import java.util.Optional;
import me.nimnakse.water_management.billing_zones.entity.BillingZone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingZoneRepository extends JpaRepository<BillingZone, Long> {
    boolean existsByOrgUnitIdAndZoneNameIgnoreCase(Long orgUnitId, String zoneName);

    boolean existsByOrgUnitIdAndZoneNameIgnoreCaseAndIdNot(Long orgUnitId, String zoneName, Long id);

    Optional<BillingZone> findTopByOrgUnitIdOrderBySequenceNumberDesc(Long orgUnitId);

    List<BillingZone> findByOrgUnitIdOrderBySequenceNumberAsc(Long orgUnitId);

    List<BillingZone> findByOrgUnitId(Long orgUnitId);
}
