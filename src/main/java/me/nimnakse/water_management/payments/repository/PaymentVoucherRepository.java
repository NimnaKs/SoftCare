package me.nimnakse.water_management.payments.repository;

import me.nimnakse.water_management.payments.entity.PaymentVoucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentVoucherRepository extends JpaRepository<PaymentVoucher, Long> {
    Page<PaymentVoucher> findByOrgUnitId(Long orgUnitId, Pageable pageable);

    @Query("select max(p.voucherNo) from PaymentVoucher p where p.orgUnitId = :orgUnitId")
    String findMaxVoucherNoByOrgUnitId(@Param("orgUnitId") Long orgUnitId);
}
