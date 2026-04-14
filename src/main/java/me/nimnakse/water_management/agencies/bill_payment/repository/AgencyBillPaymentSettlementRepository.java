package me.nimnakse.water_management.agencies.bill_payment.repository;

import java.util.List;
import me.nimnakse.water_management.agencies.bill_payment.entity.AgencyBillPaymentSettlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgencyBillPaymentSettlementRepository extends JpaRepository<AgencyBillPaymentSettlement, Long> {
    List<AgencyBillPaymentSettlement> findByAgencyBillPayment_IdOrderByCreatedAtAsc(Long agencyBillPaymentId);
}
