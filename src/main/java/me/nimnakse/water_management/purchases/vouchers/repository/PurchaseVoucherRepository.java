package me.nimnakse.water_management.purchases.vouchers.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import me.nimnakse.water_management.purchases.vouchers.entity.PurchaseVoucher;

public interface PurchaseVoucherRepository extends JpaRepository<PurchaseVoucher, Long> {
    Page<PurchaseVoucher> findByOrgUnitId(Long orgUnitId, Pageable pageable);

    @Query("select max(v.voucherNo) from PurchaseVoucher v where v.orgUnitId = :orgUnitId")
    String findMaxVoucherNoByOrgUnitId(@Param("orgUnitId") Long orgUnitId);
}
