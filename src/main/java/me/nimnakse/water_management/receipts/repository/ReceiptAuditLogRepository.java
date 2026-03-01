package me.nimnakse.water_management.receipts.repository;

import me.nimnakse.water_management.receipts.entity.ReceiptAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptAuditLogRepository extends JpaRepository<ReceiptAuditLog, Long> {
}
