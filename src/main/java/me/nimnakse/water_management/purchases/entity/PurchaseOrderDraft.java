package me.nimnakse.water_management.purchases.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Entity
@Table(name = "purchase_order_drafts")
public class PurchaseOrderDraft extends BaseEntity {
    @Column(name = "org_unit_id", nullable = false)
    private Long orgUnitId;

    @Column(name = "reference_no", nullable = false, unique = true, length = 50)
    private String referenceNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PurchaseOrderDraftStatus status = PurchaseOrderDraftStatus.PENDING;

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

    public PurchaseOrderDraftStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseOrderDraftStatus status) {
        this.status = status;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
}
