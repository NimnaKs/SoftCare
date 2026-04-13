package me.nimnakse.water_management.receipts.repository;

import me.nimnakse.water_management.receipts.entity.ReceiptSettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ReceiptSettlementRepository extends JpaRepository<ReceiptSettlement, Long> {
    List<ReceiptSettlement> findByReceipt_Id(Long receiptId);

    @Query("select coalesce(sum(rs.settledAmount), 0) from ReceiptSettlement rs join rs.receipt r where rs.invoiceId = :invoiceId and r.status = me.nimnakse.water_management.receipts.entity.ReceiptStatus.POSTED")
    BigDecimal sumPostedSettledByInvoiceId(@Param("invoiceId") Long invoiceId);

    @Query("select coalesce(sum(rs.settledAmount), 0) from ReceiptSettlement rs join rs.receipt r where rs.installmentId = :installmentId and r.status = me.nimnakse.water_management.receipts.entity.ReceiptStatus.POSTED")
    BigDecimal sumPostedSettledByInstallmentId(@Param("installmentId") Long installmentId);

    @Query("select max(rs.referenceNo) from ReceiptSettlement rs join rs.receipt r where r.orgUnitId = :orgUnitId and rs.referenceNo like concat(:prefix, '%')")
    String findMaxReferenceNoByPrefixAndOrgUnitId(@Param("orgUnitId") Long orgUnitId, @Param("prefix") String prefix);
}
