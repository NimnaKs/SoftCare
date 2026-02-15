package me.nimnakse.water_management.sales.invoices.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import me.nimnakse.water_management.common.entity.BaseEntity;
import me.nimnakse.water_management.revenue.accounts.entity.RevenueAccount;

@Entity
@Table(name = "sales_invoice_revenue_lines", indexes = {
        @Index(name = "idx_sales_inv_rev_line_account_id", columnList = "revenue_account_id")
})
public class SalesInvoiceRevenueLine extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private SalesInvoice invoice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "revenue_account_id", nullable = false)
    private RevenueAccount revenueAccount;

    @Column(name = "type_label", nullable = false)
    private String typeLabel;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "reference_no", length = 100)
    private String referenceNo;

    public SalesInvoice getInvoice() {
        return invoice;
    }

    public void setInvoice(SalesInvoice invoice) {
        this.invoice = invoice;
    }

    public RevenueAccount getRevenueAccount() {
        return revenueAccount;
    }

    public void setRevenueAccount(RevenueAccount revenueAccount) {
        this.revenueAccount = revenueAccount;
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    public void setTypeLabel(String typeLabel) {
        this.typeLabel = typeLabel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }
}
