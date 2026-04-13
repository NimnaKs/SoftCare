package me.nimnakse.water_management.receipts.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import me.nimnakse.water_management.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "receipts")
public class Receipt extends BaseEntity {
    @Column(name = "org_unit_id", nullable = false)
    private Long orgUnitId;

    @Column(name = "billing_zone_id")
    private Long billingZoneId;

    @Column(name = "connection_id")
    private Long connectionId;

    @Column(name = "receipt_no", nullable = false, length = 50)
    private String receiptNo;


    @Enumerated(EnumType.STRING)
    @Column(name = "receipt_type", nullable = false, length = 30)
    private ReceiptType receiptType;

    @Column(name = "monetary_account_id", nullable = false)
    private Long monetaryAccountId;

    @Column(name = "payment_method_id", nullable = false)
    private Long paymentMethodId;

    @Column(name = "paid_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "paid_date", nullable = false)
    private Instant paidDate;

    @Column(name = "cheque_no")
    private String chequeNo;

    @Column(name = "reference_text")
    private String referenceText;

    @Column(name = "customer_mobile_updated")
    private String customerMobileUpdated;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReceiptStatus status = ReceiptStatus.POSTED;

    @Column(name = "created_by")
    private Long createdBy;

    public Long getOrgUnitId() { return orgUnitId; }
    public void setOrgUnitId(Long orgUnitId) { this.orgUnitId = orgUnitId; }
    public Long getBillingZoneId() { return billingZoneId; }
    public void setBillingZoneId(Long billingZoneId) { this.billingZoneId = billingZoneId; }
    public Long getConnectionId() { return connectionId; }
    public void setConnectionId(Long connectionId) { this.connectionId = connectionId; }
    public String getReceiptNo() { return receiptNo; }
    public void setReceiptNo(String receiptNo) { this.receiptNo = receiptNo; }
    public ReceiptType getReceiptType() { return receiptType; }
    public void setReceiptType(ReceiptType receiptType) { this.receiptType = receiptType; }
    public Long getMonetaryAccountId() { return monetaryAccountId; }
    public void setMonetaryAccountId(Long monetaryAccountId) { this.monetaryAccountId = monetaryAccountId; }
    public Long getPaymentMethodId() { return paymentMethodId; }
    public void setPaymentMethodId(Long paymentMethodId) { this.paymentMethodId = paymentMethodId; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public Instant getPaidDate() { return paidDate; }
    public void setPaidDate(Instant paidDate) { this.paidDate = paidDate; }
    public String getChequeNo() { return chequeNo; }
    public void setChequeNo(String chequeNo) { this.chequeNo = chequeNo; }
    public String getReferenceText() { return referenceText; }
    public void setReferenceText(String referenceText) { this.referenceText = referenceText; }
    public String getCustomerMobileUpdated() { return customerMobileUpdated; }
    public void setCustomerMobileUpdated(String customerMobileUpdated) { this.customerMobileUpdated = customerMobileUpdated; }
    public ReceiptStatus getStatus() { return status; }
    public void setStatus(ReceiptStatus status) { this.status = status; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}

