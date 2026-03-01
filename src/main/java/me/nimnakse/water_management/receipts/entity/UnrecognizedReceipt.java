package me.nimnakse.water_management.receipts.entity;

import jakarta.persistence.*;
import me.nimnakse.water_management.common.entity.CreatedOnlyEntity;

@Entity
@Table(name = "unrecognized_receipts")
public class UnrecognizedReceipt extends CreatedOnlyEntity {
    @Column(name = "org_unit_id", nullable = false)
    private Long orgUnitId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receipt_id", nullable = false, unique = true)
    private Receipt receipt;

    @Column(name = "liability_account_id", nullable = false)
    private Long liabilityAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UnrecognizedReceiptStatus status = UnrecognizedReceiptStatus.OPEN;

    public Long getOrgUnitId() { return orgUnitId; }
    public void setOrgUnitId(Long orgUnitId) { this.orgUnitId = orgUnitId; }
    public Receipt getReceipt() { return receipt; }
    public void setReceipt(Receipt receipt) { this.receipt = receipt; }
    public Long getLiabilityAccountId() { return liabilityAccountId; }
    public void setLiabilityAccountId(Long liabilityAccountId) { this.liabilityAccountId = liabilityAccountId; }
    public UnrecognizedReceiptStatus getStatus() { return status; }
    public void setStatus(UnrecognizedReceiptStatus status) { this.status = status; }
}
