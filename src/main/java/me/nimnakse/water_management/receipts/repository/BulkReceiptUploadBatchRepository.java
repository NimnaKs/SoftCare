package me.nimnakse.water_management.receipts.repository;

import me.nimnakse.water_management.receipts.entity.BulkReceiptUploadBatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BulkReceiptUploadBatchRepository extends JpaRepository<BulkReceiptUploadBatch, Long> {
}
