package me.nimnakse.water_management.payments.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Entity
@Table(name = "payment_voucher_drafts",
        uniqueConstraints = @UniqueConstraint(name = "uq_payment_voucher_draft_org_ref", columnNames = {"org_unit_id", "reference_no"}))
public class PaymentVoucherDraft extends BaseEntity {
    @Column(name = "org_unit_id", nullable = false)
    private Long orgUnitId;

    @Column(name = "reference_no", nullable = false, length = 50)
    private String referenceNo;

    @Column(name = "created_by")
    private Long createdBy;

    public Long getOrgUnitId() {
        return orgUnitId;
    }

    public void setOrgUnitId(Long orgUnitId) {
        this.orgUnitId = orgUnitId;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
}
