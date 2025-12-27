package me.nimnakse.water_management.inventory.master_categories.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "inventory_master_categories")
public class InventoryMasterCategory extends BaseEntity {
    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false)
    private Integer level;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "specification_01", length = 255)
    private String specification01;

    @Column(name = "specification_02", length = 255)
    private String specification02;

    @Column(length = 50)
    private String unit;

    @Column(name = "is_leaf", nullable = false)
    private Boolean isLeaf = Boolean.FALSE;

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = Boolean.FALSE;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;
}
