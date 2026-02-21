package me.nimnakse.water_management.sales.invoices.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Entity
@Table(name = "sales_invoices", indexes = {
        @Index(name = "idx_sales_invoices_invoice_no", columnList = "invoice_no"),
        @Index(name = "idx_sales_invoices_billing_zone_id", columnList = "billing_zone_id"),
        @Index(name = "idx_sales_invoices_status", columnList = "status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_sales_invoices_org_invoice_no", columnNames = { "org_unit_id", "invoice_no" })
})
public class SalesInvoice extends BaseEntity {
    @Column(name = "invoice_no", nullable = false, length = 50)
    private String invoiceNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "sale_type", nullable = false, length = 30)
    private SaleType saleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_method", nullable = false, length = 30)
    private BillingMethod billingMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SalesInvoiceStatus status = SalesInvoiceStatus.DRAFT;

    @Column(name = "org_unit_id")
    private Long orgUnitId;

    @Column(name = "billing_zone_id")
    private Long billingZoneId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_nic")
    private String customerNic;

    @Column(name = "customer_address", length = 500)
    private String customerAddress;

    @Column(name = "customer_mobile")
    private String customerMobile;

    @Column(name = "revenue_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal revenueTotal = BigDecimal.ZERO;

    @Column(name = "sales_expense_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal salesExpenseTotal = BigDecimal.ZERO;

    @Column(name = "consumption_expense_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal consumptionExpenseTotal = BigDecimal.ZERO;

    @Column(name = "grand_total_payable", nullable = false, precision = 14, scale = 2)
    private BigDecimal grandTotalPayable = BigDecimal.ZERO;

    @Column(name = "has_inventory_issue", nullable = false)
    private Boolean hasInventoryIssue = Boolean.FALSE;

    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring = Boolean.FALSE;

    @Column(name = "recurring_enabled", nullable = false)
    private Boolean recurringEnabled = Boolean.FALSE;

    @Column(name = "down_payment", precision = 14, scale = 2)
    private BigDecimal downPayment;

    @Column(name = "number_of_installments")
    private Integer numberOfInstallments;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SalesInvoiceRevenueLine> revenueLines = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SalesInvoiceInventoryItem> inventoryItems = new ArrayList<>();

    @OneToOne(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private SalesInvoiceInventoryPolicy inventoryPolicy;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SalesInvoiceInstallment> installments = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SalesInvoiceConnection> connections = new ArrayList<>();

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public SaleType getSaleType() {
        return saleType;
    }

    public void setSaleType(SaleType saleType) {
        this.saleType = saleType;
    }

    public BillingMethod getBillingMethod() {
        return billingMethod;
    }

    public void setBillingMethod(BillingMethod billingMethod) {
        this.billingMethod = billingMethod;
    }

    public SalesInvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(SalesInvoiceStatus status) {
        this.status = status;
    }

    public Long getOrgUnitId() {
        return orgUnitId;
    }

    public void setOrgUnitId(Long orgUnitId) {
        this.orgUnitId = orgUnitId;
    }

    public Long getBillingZoneId() {
        return billingZoneId;
    }

    public void setBillingZoneId(Long billingZoneId) {
        this.billingZoneId = billingZoneId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerNic() {
        return customerNic;
    }

    public void setCustomerNic(String customerNic) {
        this.customerNic = customerNic;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getCustomerMobile() {
        return customerMobile;
    }

    public void setCustomerMobile(String customerMobile) {
        this.customerMobile = customerMobile;
    }

    public BigDecimal getRevenueTotal() {
        return revenueTotal;
    }

    public void setRevenueTotal(BigDecimal revenueTotal) {
        this.revenueTotal = revenueTotal;
    }

    public BigDecimal getSalesExpenseTotal() {
        return salesExpenseTotal;
    }

    public void setSalesExpenseTotal(BigDecimal salesExpenseTotal) {
        this.salesExpenseTotal = salesExpenseTotal;
    }

    public BigDecimal getConsumptionExpenseTotal() {
        return consumptionExpenseTotal;
    }

    public void setConsumptionExpenseTotal(BigDecimal consumptionExpenseTotal) {
        this.consumptionExpenseTotal = consumptionExpenseTotal;
    }

    public BigDecimal getGrandTotalPayable() {
        return grandTotalPayable;
    }

    public void setGrandTotalPayable(BigDecimal grandTotalPayable) {
        this.grandTotalPayable = grandTotalPayable;
    }

    public Boolean getHasInventoryIssue() {
        return hasInventoryIssue;
    }

    public void setHasInventoryIssue(Boolean hasInventoryIssue) {
        this.hasInventoryIssue = hasInventoryIssue;
    }

    public Boolean getIsRecurring() {
        return isRecurring;
    }

    public void setIsRecurring(Boolean recurring) {
        isRecurring = recurring;
    }

    public Boolean getRecurringEnabled() {
        return recurringEnabled;
    }

    public void setRecurringEnabled(Boolean recurringEnabled) {
        this.recurringEnabled = recurringEnabled;
    }

    public BigDecimal getDownPayment() {
        return downPayment;
    }

    public void setDownPayment(BigDecimal downPayment) {
        this.downPayment = downPayment;
    }

    public Integer getNumberOfInstallments() {
        return numberOfInstallments;
    }

    public void setNumberOfInstallments(Integer numberOfInstallments) {
        this.numberOfInstallments = numberOfInstallments;
    }

    public List<SalesInvoiceRevenueLine> getRevenueLines() {
        return revenueLines;
    }

    public void setRevenueLines(List<SalesInvoiceRevenueLine> revenueLines) {
        this.revenueLines = revenueLines;
    }

    public List<SalesInvoiceInventoryItem> getInventoryItems() {
        return inventoryItems;
    }

    public void setInventoryItems(List<SalesInvoiceInventoryItem> inventoryItems) {
        this.inventoryItems = inventoryItems;
    }

    public SalesInvoiceInventoryPolicy getInventoryPolicy() {
        return inventoryPolicy;
    }

    public void setInventoryPolicy(SalesInvoiceInventoryPolicy inventoryPolicy) {
        this.inventoryPolicy = inventoryPolicy;
    }

    public List<SalesInvoiceInstallment> getInstallments() {
        return installments;
    }

    public void setInstallments(List<SalesInvoiceInstallment> installments) {
        this.installments = installments;
    }

    public List<SalesInvoiceConnection> getConnections() {
        return connections;
    }

    public void setConnections(List<SalesInvoiceConnection> connections) {
        this.connections = connections;
    }
}
