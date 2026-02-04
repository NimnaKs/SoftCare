package me.nimnakse.water_management.premises.repository;

import java.util.List;
import me.nimnakse.water_management.premises.entity.Premises;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PremisesRepository extends JpaRepository<Premises, Long> {
    boolean existsByBillingZoneIdAndPremisesCode(Long billingZoneId, String premisesCode);

    boolean existsByBillingZoneIdAndPremisesCodeAndIdNot(Long billingZoneId, String premisesCode, Long id);

    boolean existsByBillingZoneIdAndSortPath(Long billingZoneId, String sortPath);

    boolean existsByParentId(Long parentId);

    List<Premises> findByBillingZoneIdOrderBySortPathAsc(Long billingZoneId);

    List<Premises> findByBillingZoneIdAndParentId(Long billingZoneId, Long parentId);

    List<Premises> findByBillingZoneIdAndParentIdIsNull(Long billingZoneId);
}
