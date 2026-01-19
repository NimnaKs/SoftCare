package me.nimnakse.water_management.fixed_assets.initial_stocks.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Entity
@Table(name = "fixed_asset_initial_stocks", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"org_unit_id", "batch_no"})
})
public class FixedAssetInitialStock extends BaseEntity {
    @Column(name = "org_unit_id", nullable = false)
    private Long orgUnitId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "batch_no", nullable = false, length = 50)
    private String batchNo;

    @Column(name = "batch_sequence", nullable = false)
    private Integer batchSequence;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal quantity;

    public Long getOrgUnitId() {
        return orgUnitId;
    }

    public void setOrgUnitId(Long orgUnitId) {
        this.orgUnitId = orgUnitId;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public Integer getBatchSequence() {
        return batchSequence;
    }

    public void setBatchSequence(Integer batchSequence) {
        this.batchSequence = batchSequence;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
}
