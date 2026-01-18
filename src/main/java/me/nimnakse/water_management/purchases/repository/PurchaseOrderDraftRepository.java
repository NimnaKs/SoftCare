package me.nimnakse.water_management.purchases.repository;

import java.util.List;
import me.nimnakse.water_management.purchases.entity.PurchaseOrderDraft;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseOrderDraftRepository extends JpaRepository<PurchaseOrderDraft, Long> {
    boolean existsByOrgUnitIdAndReferenceNo(Long orgUnitId, String referenceNo);

    @Query("select max(d.referenceNo) from PurchaseOrderDraft d where d.orgUnitId = :orgUnitId")
    String findMaxReferenceNoByOrgUnitId(@Param("orgUnitId") Long orgUnitId);

    Page<PurchaseOrderDraft> findByOrgUnitId(Long orgUnitId, Pageable pageable);

    List<PurchaseOrderDraft> findByOrgUnitId(Long orgUnitId);
}
