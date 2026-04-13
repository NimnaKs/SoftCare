package me.nimnakse.water_management.receipts.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_type", nullable = false, length = 30)
    private ReceiptSettlementType settlementType = ReceiptSettlementType.INVOICE;

    @Column(name = "liability_account_id")
    private Long liabilityAccountId;

    @Column(name = "reference_no", length = 50)
    private String referenceNo;

    public Receipt getReceipt() { return receipt; }
    public void setReceipt(Receipt receipt) { this.receipt = receipt; }
    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public Long getInstallmentId() { return installmentId; }
    public void setInstallmentId(Long installmentId) { this.installmentId = installmentId; }
    public BigDecimal getSettledAmount() { return settledAmount; }
    public void setSettledAmount(BigDecimal settledAmount) { this.settledAmount = settledAmount; }
    public ReceiptSettlementType getSettlementType() { return settlementType; }
    public void setSettlementType(ReceiptSettlementType settlementType) { this.settlementType = settlementType; }
    public Long getLiabilityAccountId() { return liabilityAccountId; }
    public void setLiabilityAccountId(Long liabilityAccountId) { this.liabilityAccountId = liabilityAccountId; }
    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
}
