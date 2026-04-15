package me.nimnakse.water_management.meter_reader_assignments.repository;

import java.util.List;
import me.nimnakse.water_management.meter_reader_assignments.entity.MeterReaderZoneAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeterReaderZoneAssignmentRepository extends JpaRepository<MeterReaderZoneAssignment, Long> {
    List<MeterReaderZoneAssignment> findByOrgUnit_IdOrderByAssignedFromDesc(Long orgUnitId);
    List<MeterReaderZoneAssignment> findByOrgUnit_IdAndReaderUser_IdAndBillingZoneIdAndAssignedToIsNull(Long orgUnitId, Long readerUserId, Long billingZoneId);
    List<MeterReaderZoneAssignment> findByOrgUnit_IdAndBillingZoneIdAndAssignedToIsNull(Long orgUnitId, Long billingZoneId);
}
