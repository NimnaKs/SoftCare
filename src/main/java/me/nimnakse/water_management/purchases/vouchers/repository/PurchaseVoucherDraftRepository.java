package me.nimnakse.water_management.purchases.vouchers.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import me.nimnakse.water_management.purchases.vouchers.entity.PurchaseVoucherDraft;

public interface PurchaseVoucherDraftRepository extends JpaRepository<PurchaseVoucherDraft, Long> {
    Page<PurchaseVoucherDraft> findByOrgUnitId(Long orgUnitId, Pageable pageable);

    @Query("select max(d.referenceNo) from PurchaseVoucherDraft d where d.orgUnitId = :orgUnitId")
    String findMaxReferenceNoByOrgUnitId(@Param("orgUnitId") Long orgUnitId);
}
