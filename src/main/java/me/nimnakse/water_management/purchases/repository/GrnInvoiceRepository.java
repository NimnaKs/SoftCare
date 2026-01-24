package me.nimnakse.water_management.purchases.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import me.nimnakse.water_management.purchases.entity.GrnInvoice;

public interface GrnInvoiceRepository extends JpaRepository<GrnInvoice, Long> {
    Page<GrnInvoice> findByOrgUnitId(Long orgUnitId, Pageable pageable);

    boolean existsByGrnNo(String grnNo);

    @Query("select max(g.grnNo) from GrnInvoice g where g.orgUnitId = :orgUnitId")
    String findMaxGrnNoByOrgUnitId(@Param("orgUnitId") Long orgUnitId);

    List<GrnInvoice> findByOrgUnitIdAndIdNotIn(Long orgUnitId, List<Long> userIds);

    List<GrnInvoice> findByOrgUnitId(Long orgUnitId);
}
