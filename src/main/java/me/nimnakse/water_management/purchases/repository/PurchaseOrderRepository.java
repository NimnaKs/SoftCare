package me.nimnakse.water_management.purchases.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import me.nimnakse.water_management.purchases.entity.PurchaseOrder;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Page<PurchaseOrder> findByOrgUnitId(Long orgUnitId, Pageable pageable);

    @Query("select max(p.purchaseOrderNo) from PurchaseOrder p where p.orgUnitId = :orgUnitId")
    String findMaxPurchaseOrderNoByOrgUnitId(@Param("orgUnitId") Long orgUnitId);
}
