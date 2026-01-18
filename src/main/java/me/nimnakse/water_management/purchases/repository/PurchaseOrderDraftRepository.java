package me.nimnakse.water_management.purchases.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import me.nimnakse.water_management.purchases.entity.PurchaseOrderDraft;

public interface PurchaseOrderDraftRepository extends JpaRepository<PurchaseOrderDraft, Long> {
    boolean existsByReferenceNo(String referenceNo);

    Page<PurchaseOrderDraft> findByOrgUnitId(Long orgUnitId, Pageable pageable);

    List<PurchaseOrderDraft> findByOrgUnitId(Long orgUnitId);
}
