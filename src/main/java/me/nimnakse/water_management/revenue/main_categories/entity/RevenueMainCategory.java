package me.nimnakse.water_management.revenue.main_categories.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import me.nimnakse.water_management.common.entity.BaseEntity;
import me.nimnakse.water_management.revenue.RevenueCustomerType;

@Entity
@Table(name = "revenue_main_categories")
public class RevenueMainCategory extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false)
    private RevenueCustomerType customerType;

    @Column(name = "code", nullable = false, unique = true)
    private Integer code;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = Boolean.TRUE;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    public RevenueCustomerType getCustomerType() {
        return customerType;
    }

    public void setCustomerType(RevenueCustomerType customerType) {
        this.customerType = customerType;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
