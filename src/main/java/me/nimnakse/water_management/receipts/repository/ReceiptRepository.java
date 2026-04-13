package me.nimnakse.water_management.receipts.repository;

import me.nimnakse.water_management.receipts.entity.Receipt;
import me.nimnakse.water_management.receipts.entity.ReceiptStatus;
import me.nimnakse.water_management.receipts.entity.ReceiptType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    Optional<Receipt> findByReceiptNo(String receiptNo);

    @Query("""
            select r from Receipt r
            where (:orgUnitId is null or r.orgUnitId = :orgUnitId)
              and (:connectionId is null or r.connectionId = :connectionId)
              and (:receiptNo is null or lower(r.receiptNo) like concat('%', lower(:receiptNo), '%'))
              and (:status is null or r.status = :status)
              and (:receiptType is null or r.receiptType = :receiptType)
              and (:dateFrom is null or r.paidDate >= :dateFrom)
              and (:dateTo is null or r.paidDate < :dateTo)
            """)
    Page<Receipt> search(
            @Param("orgUnitId") Long orgUnitId,
            @Param("connectionId") Long connectionId,
            @Param("receiptNo") String receiptNo,
            @Param("status") ReceiptStatus status,
            @Param("receiptType") ReceiptType receiptType,
            @Param("dateFrom") Instant dateFrom,
            @Param("dateTo") Instant dateTo,
            Pageable pageable);

    @Query("select max(r.receiptNo) from Receipt r where r.orgUnitId = :orgUnitId and r.receiptNo like concat(:prefix, '%')")
    String findMaxReceiptNoByPrefixAndOrgUnitId(@Param("orgUnitId") Long orgUnitId, @Param("prefix") String prefix);
}
