package me.nimnakse.water_management.utility_bills.repository;
import java.util.List; import java.util.Optional; import me.nimnakse.water_management.utility_bills.entity.UtilityBill; import me.nimnakse.water_management.utility_bills.entity.UtilityBillStatus; import org.springframework.data.jpa.repository.JpaRepository;
public interface UtilityBillRepository extends JpaRepository<UtilityBill, Long> {
 Optional<UtilityBill> findTopByOrgUnitIdAndBillingZoneIdAndAccountNumberAndBillingMonthAndDeletedAtIsNullOrderByCreatedAtDesc(Long orgUnitId, Long billingZoneId, String accountNumber, String billingMonth);
 Optional<UtilityBill> findTopByOrgUnitIdAndBillingZoneIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long orgUnitId, Long billingZoneId);
 List<UtilityBill> findByOrgUnitIdAndStatusAndDeletedAtIsNullOrderByBillingZoneIdAscAccountNumberAsc(Long orgUnitId, UtilityBillStatus status);
}