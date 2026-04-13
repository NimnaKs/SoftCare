package me.nimnakse.water_management.receipts.repository;

import me.nimnakse.water_management.receipts.entity.UnrecognizedReceipt;
import me.nimnakse.water_management.receipts.entity.UnrecognizedReceiptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UnrecognizedReceiptRepository extends JpaRepository<UnrecognizedReceipt, Long> {
    Optional<UnrecognizedReceipt> findByReceipt_Id(Long receiptId);
    List<UnrecognizedReceipt> findByReceipt_IdIn(Collection<Long> receiptIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update UnrecognizedReceipt u
               set u.allocatedAmount = coalesce(u.allocatedAmount, 0) + :allocatedAmount,
                   u.status = :status
             where u.id = :id
            """)
    int applyAllocation(
            @Param("id") Long id,
            @Param("allocatedAmount") BigDecimal allocatedAmount,
            @Param("status") UnrecognizedReceiptStatus status);
}