package me.nimnakse.water_management.receipts.entity;

import jakarta.persistence.*;
import me.nimnakse.water_management.common.entity.CreatedOnlyEntity;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "bulk_receipt_upload_rows")
public class BulkReceiptUploadRow extends CreatedOnlyEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private BulkReceiptUploadBatch batch;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "paid_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "paid_date")
    private Instant paidDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BulkReceiptUploadRowStatus status = BulkReceiptUploadRowStatus.PENDING;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    public BulkReceiptUploadBatch getBatch() { return batch; }
    public void setBatch(BulkReceiptUploadBatch batch) { this.batch = batch; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public Instant getPaidDate() { return paidDate; }
    public void setPaidDate(Instant paidDate) { this.paidDate = paidDate; }
    public BulkReceiptUploadRowStatus getStatus() { return status; }
    public void setStatus(BulkReceiptUploadRowStatus status) { this.status = status; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
