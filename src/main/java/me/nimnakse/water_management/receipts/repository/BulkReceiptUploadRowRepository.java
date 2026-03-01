package me.nimnakse.water_management.receipts.repository;

import me.nimnakse.water_management.receipts.entity.BulkReceiptUploadRow;
import me.nimnakse.water_management.receipts.entity.BulkReceiptUploadRowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BulkReceiptUploadRowRepository extends JpaRepository<BulkReceiptUploadRow, Long> {
    List<BulkReceiptUploadRow> findByBatch_IdAndStatus(Long batchId, BulkReceiptUploadRowStatus status);
    long countByBatch_IdAndStatus(Long batchId, BulkReceiptUploadRowStatus status);
    long countByBatch_Id(Long batchId);
    Page<BulkReceiptUploadRow> findByBatch_Id(Long batchId, Pageable pageable);
}
