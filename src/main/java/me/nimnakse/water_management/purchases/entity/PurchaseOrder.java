package me.nimnakse.water_management.purchases.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder extends BaseEntity {
    @Column(name = "org_unit_id", nullable = false)
    private Long orgUnitId;

    @Column(name = "purchase_order_no", nullable = false, unique = true, length = 50)
    private String purchaseOrderNo;

    @Column(name = "draft_id")
    private Long draftId;

    @Column(name = "supplier_id")
    private Long supplierId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PurchaseOrderStatus status = PurchaseOrderStatus.PENDING;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "created_by")
    private Long createdBy;

    public Long getOrgUnitId() {
        return orgUnitId;
    }

    public void setOrgUnitId(Long orgUnitId) {
        this.orgUnitId = orgUnitId;
    }

    public String getPurchaseOrderNo() {
        return purchaseOrderNo;
    }

    public void setPurchaseOrderNo(String purchaseOrderNo) {
        this.purchaseOrderNo = purchaseOrderNo;
    }

    public Long getDraftId() {
        return draftId;
    }

    public void setDraftId(Long draftId) {
        this.draftId = draftId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public PurchaseOrderStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseOrderStatus status) {
        this.status = status;
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
}
