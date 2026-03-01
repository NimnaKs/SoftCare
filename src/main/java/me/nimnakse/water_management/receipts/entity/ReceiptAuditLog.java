package me.nimnakse.water_management.receipts.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import me.nimnakse.water_management.common.entity.CreatedOnlyEntity;

@Entity
@Table(name = "receipt_audit_logs")
public class ReceiptAuditLog extends CreatedOnlyEntity {
    @Column(name = "receipt_id")
    private Long receiptId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 40)
    private ReceiptAuditAction action;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "performed_by")
    private Long performedBy;

    public Long getReceiptId() { return receiptId; }
    public void setReceiptId(Long receiptId) { this.receiptId = receiptId; }
    public ReceiptAuditAction getAction() { return action; }
    public void setAction(ReceiptAuditAction action) { this.action = action; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getPerformedBy() { return performedBy; }
    public void setPerformedBy(Long performedBy) { this.performedBy = performedBy; }
}
