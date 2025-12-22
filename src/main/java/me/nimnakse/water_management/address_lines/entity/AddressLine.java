package me.nimnakse.water_management.address_lines.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Entity
@Table(name = "address_lines")
public class AddressLine extends BaseEntity {
    @Column(name = "org_unit_id", nullable = false)
    private Long orgUnitId;

    @Column(nullable = false)
    private Integer level;

    @Column(nullable = false)
    private String name;

    @Column(name = "parent_line1_id")
    private Long parentLine1Id;

    @Column(name = "parent_line2_id")
    private Long parentLine2Id;

    @Column(name = "parent_line3_id")
    private Long parentLine3Id;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "internal_code")
    private String internalCode;

    public Integer getLevel() {
        return level;
    }

    public Long getOrgUnitId() {
        return orgUnitId;
    }

    public void setOrgUnitId(Long orgUnitId) {
        this.orgUnitId = orgUnitId;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentLine1Id() {
        return parentLine1Id;
    }

    public void setParentLine1Id(Long parentLine1Id) {
        this.parentLine1Id = parentLine1Id;
    }

    public Long getParentLine2Id() {
        return parentLine2Id;
    }

    public void setParentLine2Id(Long parentLine2Id) {
        this.parentLine2Id = parentLine2Id;
    }

    public Long getParentLine3Id() {
        return parentLine3Id;
    }

    public void setParentLine3Id(Long parentLine3Id) {
        this.parentLine3Id = parentLine3Id;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getInternalCode() {
        return internalCode;
    }

    public void setInternalCode(String internalCode) {
        this.internalCode = internalCode;
    }
}
