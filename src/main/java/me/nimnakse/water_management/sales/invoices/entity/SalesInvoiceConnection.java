package me.nimnakse.water_management.sales.invoices.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Entity
@Table(name = "sales_invoice_connections", indexes = {
        @Index(name = "idx_sales_inv_connections_account_no", columnList = "account_number"),
        @Index(name = "idx_sales_inv_connections_connection_id", columnList = "connection_id")
})
public class SalesInvoiceConnection extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private SalesInvoice invoice;

    @Column(name = "connection_id", nullable = false)
    private Long connectionId;

    @Column(name = "billing_zone_id")
    private Long billingZoneId;

    @Column(name = "premises_no", nullable = false)
    private String premisesNo;

    @Column(name = "connection_no", nullable = false)
    private String connectionNo;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    public SalesInvoice getInvoice() {
        return invoice;
    }

    public void setInvoice(SalesInvoice invoice) {
        this.invoice = invoice;
    }

    public Long getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(Long connectionId) {
        this.connectionId = connectionId;
    }

    public Long getBillingZoneId() {
        return billingZoneId;
    }

    public void setBillingZoneId(Long billingZoneId) {
        this.billingZoneId = billingZoneId;
    }

    public String getPremisesNo() {
        return premisesNo;
    }

    public void setPremisesNo(String premisesNo) {
        this.premisesNo = premisesNo;
    }

    public String getConnectionNo() {
        return connectionNo;
    }

    public void setConnectionNo(String connectionNo) {
        this.connectionNo = connectionNo;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}
