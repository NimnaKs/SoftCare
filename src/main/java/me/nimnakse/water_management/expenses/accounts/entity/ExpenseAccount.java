package me.nimnakse.water_management.expenses.accounts.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import me.nimnakse.water_management.common.entity.BaseEntity;
import me.nimnakse.water_management.expenses.main_categories.entity.ExpenseMainCategory;

@Entity
@Table(name = "expense_accounts")
public class ExpenseAccount extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "main_category_id", nullable = false)
    private ExpenseMainCategory mainCategory;

    @Column(name = "account_code", nullable = false, unique = true)
    private String accountCode;

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

    public ExpenseMainCategory getMainCategory() {
        return mainCategory;
    }

    public void setMainCategory(ExpenseMainCategory mainCategory) {
        this.mainCategory = mainCategory;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountNumber) {
        this.accountCode = accountNumber;
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
