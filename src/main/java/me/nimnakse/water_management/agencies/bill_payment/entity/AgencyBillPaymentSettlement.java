package me.nimnakse.water_management.agencies.bill_payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import me.nimnakse.water_management.common.entity.CreatedOnlyEntity;

@Getter
@Setter
@Entity
@Table(name = "agency_bill_payment_settlements")
public class AgencyBillPaymentSettlement extends CreatedOnlyEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agency_bill_payment_id", nullable = false)
    private AgencyBillPayment agencyBillPayment;

    @Column(name = "agency_bill_payment_id", insertable = false, updatable = false)
    private Long agencyBillPaymentId;

    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "installment_id")
    private Long installmentId;

    @Column(name = "settled_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal settledAmount;

    @Column(name = "reference_no", length = 50)
    private String referenceNo;
}
