package me.nimnakse.water_management.inventory.templates.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Entity
@Table(name = "inventory_templates")
public class InventoryTemplate extends BaseEntity {
    @Column(name = "template_code", nullable = false, unique = true, length = 50)
    private String templateCode;

    @Column(name = "level_one_category_id", nullable = false)
    private Long levelOneCategoryId;

    @Column(name = "level_two_category_id", nullable = false)
    private Long levelTwoCategoryId;

    @Column(name = "level_three_category_id", nullable = false)
    private Long levelThreeCategoryId;

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public Long getLevelOneCategoryId() {
        return levelOneCategoryId;
    }

    public void setLevelOneCategoryId(Long levelOneCategoryId) {
        this.levelOneCategoryId = levelOneCategoryId;
    }

    public Long getLevelTwoCategoryId() {
        return levelTwoCategoryId;
    }

    public void setLevelTwoCategoryId(Long levelTwoCategoryId) {
        this.levelTwoCategoryId = levelTwoCategoryId;
    }

    public Long getLevelThreeCategoryId() {
        return levelThreeCategoryId;
    }

    public void setLevelThreeCategoryId(Long levelThreeCategoryId) {
        this.levelThreeCategoryId = levelThreeCategoryId;
    }
}
