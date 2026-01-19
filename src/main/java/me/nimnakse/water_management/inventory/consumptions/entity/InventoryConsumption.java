package me.nimnakse.water_management.inventory.consumptions.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Entity
@Table(name = "inventory_consumptions")
public class InventoryConsumption extends BaseEntity {
    @Column(name = "inventory_template_id", nullable = false)
    private Long inventoryTemplateId;

    @Column(name = "expense_account_id")
    private Long expenseAccountId;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal quantity;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "consumed_at", nullable = false)
    private LocalDate consumedAt;

    @Column(name = "reference_no", length = 100)
    private String referenceNo;

    @Column(name = "batch_no", length = 50)
    private String batchNo;

    @Column(length = 255)
    private String description;

    public Long getInventoryTemplateId() {
        return inventoryTemplateId;
    }

    public void setInventoryTemplateId(Long inventoryTemplateId) {
        this.inventoryTemplateId = inventoryTemplateId;
    }

    public Long getExpenseAccountId() {
        return expenseAccountId;
    }

    public void setExpenseAccountId(Long expenseAccountId) {
        this.expenseAccountId = expenseAccountId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDate getConsumedAt() {
        return consumedAt;
    }

    public void setConsumedAt(LocalDate consumedAt) {
        this.consumedAt = consumedAt;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
