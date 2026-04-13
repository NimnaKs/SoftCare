package me.nimnakse.water_management.receipts.repository;

import me.nimnakse.water_management.receipts.entity.UnrecognizedReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UnrecognizedReceiptRepository extends JpaRepository<UnrecognizedReceipt, Long> {
    Optional<UnrecognizedReceipt> findByReceipt_Id(Long receiptId);
    List<UnrecognizedReceipt> findByReceipt_IdIn(Collection<Long> receiptIds);
}