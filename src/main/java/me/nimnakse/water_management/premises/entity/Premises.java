package me.nimnakse.water_management.premises.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Entity
@Table(name = "premises")
public class Premises extends BaseEntity {
    @Column(name = "billing_zone_id", nullable = false)
    private Long billingZoneId;

    @Column(name = "premises_code", nullable = false)
    private String premisesCode;

    @Column(name = "sort_path", nullable = false)
    private String sortPath;

    @Column(name = "parent_id")
    private Long parentId;

    public Long getBillingZoneId() {
        return billingZoneId;
    }

    public void setBillingZoneId(Long billingZoneId) {
        this.billingZoneId = billingZoneId;
    }

    public String getPremisesCode() {
        return premisesCode;
    }

    public void setPremisesCode(String premisesCode) {
        this.premisesCode = premisesCode;
    }

    public String getSortPath() {
        return sortPath;
    }

    public void setSortPath(String sortPath) {
        this.sortPath = sortPath;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
