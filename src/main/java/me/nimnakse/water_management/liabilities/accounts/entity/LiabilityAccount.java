package me.nimnakse.water_management.liabilities.accounts.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import me.nimnakse.water_management.common.entity.BaseEntity;
import me.nimnakse.water_management.liabilities.main_categories.entity.LiabilityMainCategory;

@Entity
@Table(name = "liability_accounts")
public class LiabilityAccount extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "main_category_id", nullable = false)
    private LiabilityMainCategory mainCategory;

    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = Boolean.FALSE;

    @Column(name = "function_key", length = 100)
    private String functionKey;

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = Boolean.FALSE;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    public LiabilityMainCategory getMainCategory() {
        return mainCategory;
    }

    public void setMainCategory(LiabilityMainCategory mainCategory) {
        this.mainCategory = mainCategory;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
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

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean aDefault) {
        isDefault = aDefault;
    }

    public String getFunctionKey() {
        return functionKey;
    }

    public void setFunctionKey(String functionKey) {
        this.functionKey = functionKey;
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
