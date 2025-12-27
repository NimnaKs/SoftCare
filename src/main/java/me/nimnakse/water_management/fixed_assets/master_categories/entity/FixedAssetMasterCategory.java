package me.nimnakse.water_management.fixed_assets.master_categories.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Entity
@Table(name = "fixed_asset_master_categories")
public class FixedAssetMasterCategory extends BaseEntity {
    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "specification_01", length = 255)
    private String specification01;

    @Column(name = "specification_02", length = 255)
    private String specification02;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "is_leaf", nullable = false)
    private Boolean isLeaf = Boolean.FALSE;

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = Boolean.FALSE;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getLevel() {
        return level;
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

    public String getSpecification01() {
        return specification01;
    }

    public void setSpecification01(String specification01) {
        this.specification01 = specification01;
    }

    public String getSpecification02() {
        return specification02;
    }

    public void setSpecification02(String specification02) {
        this.specification02 = specification02;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Boolean getIsLeaf() {
        return isLeaf;
    }

    public void setIsLeaf(Boolean leaf) {
        isLeaf = leaf;
    }

    public Boolean getIsSystem() {
        return isSystem;
    }

    public void setIsSystem(Boolean system) {
        isSystem = system;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }
}
