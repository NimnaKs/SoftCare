package me.nimnakse.water_management.sales.invoices.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Entity
@Table(name = "sales_invoice_inventory_items")
public class SalesInvoiceInventoryItem extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private SalesInvoice invoice;

    @Column(name = "category1")
    private String category1;

    @Column(name = "category2")
    private String category2;

    @Column(name = "category3")
    private String category3;

    @Column(name = "description_spec", length = 500)
    private String descriptionSpec;

    @Column(name = "qty", nullable = false, precision = 14, scale = 3)
    private BigDecimal qty = BigDecimal.ZERO;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "unit_cost", nullable = false, precision = 14, scale = 2)
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    public SalesInvoice getInvoice() {
        return invoice;
    }

    public void setInvoice(SalesInvoice invoice) {
        this.invoice = invoice;
    }

    public String getCategory1() {
        return category1;
    }

    public void setCategory1(String category1) {
        this.category1 = category1;
    }

    public String getCategory2() {
        return category2;
    }

    public void setCategory2(String category2) {
        this.category2 = category2;
    }

    public String getCategory3() {
        return category3;
    }

    public void setCategory3(String category3) {
        this.category3 = category3;
    }

    public String getDescriptionSpec() {
        return descriptionSpec;
    }

    public void setDescriptionSpec(String descriptionSpec) {
        this.descriptionSpec = descriptionSpec;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
