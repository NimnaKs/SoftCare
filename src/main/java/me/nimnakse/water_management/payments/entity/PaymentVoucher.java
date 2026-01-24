package me.nimnakse.water_management.payments.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Entity
@Table(name = "payment_vouchers")
public class PaymentVoucher extends BaseEntity {
    @Column(name = "org_unit_id", nullable = false)
    private Long orgUnitId;

    @Column(name = "voucher_no", nullable = false, length = 50)
    private String voucherNo;

    @Column(name = "draft_id")
    private Long draftId;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "payment_date")
    private java.time.LocalDate paymentDate;

    @Column(name = "fund_source_id")
    private Long fundSourceId;

    @Column(name = "payment_method_id")
    private Long paymentMethodId;

    @Column(name = "created_by")
    private Long createdBy;

    public Long getOrgUnitId() {
        return orgUnitId;
    }

    public void setOrgUnitId(Long orgUnitId) {
        this.orgUnitId = orgUnitId;
    }

    public String getVoucherNo() {
        return voucherNo;
    }

    public void setVoucherNo(String voucherNo) {
        this.voucherNo = voucherNo;
    }

    public Long getDraftId() {
        return draftId;
    }

    public void setDraftId(Long draftId) {
        this.draftId = draftId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public java.time.LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(java.time.LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Long getFundSourceId() {
        return fundSourceId;
    }

    public void setFundSourceId(Long fundSourceId) {
        this.fundSourceId = fundSourceId;
    }

    public Long getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(Long paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }
}
