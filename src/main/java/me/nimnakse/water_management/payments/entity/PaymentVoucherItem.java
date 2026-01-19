package me.nimnakse.water_management.payments.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Entity
@Table(name = "payment_voucher_items")
public class PaymentVoucherItem extends BaseEntity {
    @Column(name = "voucher_id", nullable = false)
    private Long voucherId;

    @Column(name = "expense_account_id", nullable = false)
    private Long expenseAccountId;

    @Column(length = 255)
    private String description;

    @Column(name = "total_amount", precision = 14, scale = 2)
    private BigDecimal totalAmount;

    public Long getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(Long voucherId) {
        this.voucherId = voucherId;
    }

    public Long getExpenseAccountId() {
        return expenseAccountId;
    }

    public void setExpenseAccountId(Long expenseAccountId) {
        this.expenseAccountId = expenseAccountId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
