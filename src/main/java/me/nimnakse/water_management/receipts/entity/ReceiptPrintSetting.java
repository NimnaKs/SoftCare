package me.nimnakse.water_management.receipts.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Entity
@Table(name = "receipt_print_settings")
public class ReceiptPrintSetting extends BaseEntity {
    @Column(name = "org_unit_id", nullable = false, unique = true)
    private Long orgUnitId;

    @Column(name = "show_settlements_on_print", nullable = false)
    private Boolean showSettlementsOnPrint = Boolean.FALSE;

    public Long getOrgUnitId() { return orgUnitId; }
    public void setOrgUnitId(Long orgUnitId) { this.orgUnitId = orgUnitId; }
    public Boolean getShowSettlementsOnPrint() { return showSettlementsOnPrint; }
    public void setShowSettlementsOnPrint(Boolean showSettlementsOnPrint) { this.showSettlementsOnPrint = showSettlementsOnPrint; }
}
