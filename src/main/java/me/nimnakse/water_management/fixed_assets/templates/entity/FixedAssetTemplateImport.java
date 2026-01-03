package me.nimnakse.water_management.fixed_assets.templates.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Entity
@Table(name = "fixed_asset_template_imports",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"org_unit_id", "template_id"})})
public class FixedAssetTemplateImport extends BaseEntity {
    @Column(name = "org_unit_id", nullable = false)
    private Long orgUnitId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

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
}
