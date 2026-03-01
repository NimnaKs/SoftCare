package me.nimnakse.water_management.receipts.entity;

import jakarta.persistence.*;
import me.nimnakse.water_management.common.entity.CreatedOnlyEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "receipt_settlements")
public class ReceiptSettlement extends CreatedOnlyEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receipt_id", nullable = false)
    private Receipt receipt;

    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "installment_id")
    private Long installmentId;

    @Column(name = "settled_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal settledAmount;

    public Receipt getReceipt() { return receipt; }
    public void setReceipt(Receipt receipt) { this.receipt = receipt; }
    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public Long getInstallmentId() { return installmentId; }
    public void setInstallmentId(Long installmentId) { this.installmentId = installmentId; }
    public BigDecimal getSettledAmount() { return settledAmount; }
    public void setSettledAmount(BigDecimal settledAmount) { this.settledAmount = settledAmount; }
}
