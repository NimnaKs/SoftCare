package me.nimnakse.water_management.purchases.vouchers.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Entity
@Table(name = "purchase_voucher_items")
public class PurchaseVoucherItem extends BaseEntity {
    @Column(name = "voucher_id", nullable = false)
    private Long voucherId;

    @Column(name = "grn_invoice_id", nullable = false)
    private Long grnInvoiceId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    public Long getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(Long voucherId) {
        this.voucherId = voucherId;
    }

    public Long getGrnInvoiceId() {
        return grnInvoiceId;
    }

    public void setGrnInvoiceId(Long grnInvoiceId) {
        this.grnInvoiceId = grnInvoiceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
