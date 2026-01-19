package me.nimnakse.water_management.payments.repository;

import java.util.List;
import me.nimnakse.water_management.payments.entity.PaymentVoucherDraft;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentVoucherDraftRepository extends JpaRepository<PaymentVoucherDraft, Long> {
    @Query("select max(d.referenceNo) from PaymentVoucherDraft d where d.orgUnitId = :orgUnitId")
    String findMaxReferenceNoByOrgUnitId(@Param("orgUnitId") Long orgUnitId);

    Page<PaymentVoucherDraft> findByOrgUnitId(Long orgUnitId, Pageable pageable);

    List<PaymentVoucherDraft> findByOrgUnitId(Long orgUnitId);
}
