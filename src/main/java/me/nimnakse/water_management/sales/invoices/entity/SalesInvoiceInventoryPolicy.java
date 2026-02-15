package me.nimnakse.water_management.sales.invoices.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Entity
@Table(name = "sales_invoice_inventory_policy")
public class SalesInvoiceInventoryPolicy extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false, unique = true)
    private SalesInvoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_as", nullable = false, length = 40)
    private SalesInvoiceInventoryRecordAs recordAs;

    @Column(name = "charged_from_customer", nullable = false)
    private Boolean chargedFromCustomer = Boolean.FALSE;

    public SalesInvoice getInvoice() {
        return invoice;
    }

    public void setInvoice(SalesInvoice invoice) {
        this.invoice = invoice;
    }

    public SalesInvoiceInventoryRecordAs getRecordAs() {
        return recordAs;
    }

    public void setRecordAs(SalesInvoiceInventoryRecordAs recordAs) {
        this.recordAs = recordAs;
    }

    public Boolean getChargedFromCustomer() {
        return chargedFromCustomer;
    }

    public void setChargedFromCustomer(Boolean chargedFromCustomer) {
        this.chargedFromCustomer = chargedFromCustomer;
    }
}
