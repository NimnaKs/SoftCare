package me.nimnakse.water_management.receipts.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import me.nimnakse.water_management.common.entity.CreatedOnlyEntity;

@Entity
@Table(name = "bulk_receipt_upload_batches")
public class BulkReceiptUploadBatch extends CreatedOnlyEntity {
    @Column(name = "org_unit_id", nullable = false)
    private Long orgUnitId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BulkReceiptUploadBatchStatus status = BulkReceiptUploadBatchStatus.UPLOADED;

    @Column(name = "created_by")
    private Long createdBy;

    public Long getOrgUnitId() { return orgUnitId; }
    public void setOrgUnitId(Long orgUnitId) { this.orgUnitId = orgUnitId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public BulkReceiptUploadBatchStatus getStatus() { return status; }
    public void setStatus(BulkReceiptUploadBatchStatus status) { this.status = status; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}
