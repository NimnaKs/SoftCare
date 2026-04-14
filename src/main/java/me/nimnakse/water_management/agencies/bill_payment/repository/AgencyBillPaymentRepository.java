package me.nimnakse.water_management.agencies.bill_payment.repository;

import java.util.List;
import me.nimnakse.water_management.agencies.bill_payment.entity.AgencyBillPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgencyBillPaymentRepository extends JpaRepository<AgencyBillPayment, Long> {
    List<AgencyBillPayment> findByAgencyIdOrderByCreatedAtDesc(Long agencyId);

    List<AgencyBillPayment> findAllByOrderByCreatedAtDesc();
}
