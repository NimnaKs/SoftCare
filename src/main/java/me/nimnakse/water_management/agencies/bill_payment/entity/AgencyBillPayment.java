package me.nimnakse.water_management.agencies.bill_payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import me.nimnakse.water_management.agencies.entity.Agency;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "agency_bill_payments")
public class AgencyBillPayment extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;

    @Column(name = "agency_id", insertable = false, updatable = false)
    private Long agencyId;

    @Column(name = "cash_account_id", nullable = false)
    private Long cashAccountId;

    @Column(name = "cash_account_name", nullable = false, length = 255)
    private String cashAccountName;

    @Column(name = "payment_method_id", nullable = false)
    private Long paymentMethodId;

    @Column(name = "payment_method_name", nullable = false, length = 255)
    private String paymentMethodName;

    @Column(name = "reference_text", nullable = false, length = 255)
    private String referenceText;

    @Column(name = "paid_date", nullable = false)
    private LocalDate paidDate;

    @Column(name = "bill_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal billAmount;

    @Column(name = "service_charge_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal serviceChargeAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;
}
