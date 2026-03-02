package me.nimnakse.water_management.subscription_payments.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import me.nimnakse.water_management.agencies.entity.Agency;
import me.nimnakse.water_management.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "subscription_payment_requests")
public class SubscriptionPaymentRequest  extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;

    @Column(name = "cash_account_label", nullable = false, length = 255)
    private String cashAccountLabel;

    @Column(name = "payment_method_id", nullable = false)
    private Long paymentMethodId;

    @Column(name = "reference_text", nullable = false, length = 255)
    private String referenceText;

    @Column(name = "paid_date", nullable = false)
    private LocalDate paidDate;

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SubscriptionPaymentRequestStatus status = SubscriptionPaymentRequestStatus.PENDING_CONFIRMATION;

    @Column(name = "attachment_name", length = 255)
    private String attachmentName;

    @Column(name = "attachment_path", length = 500)
    private String attachmentPath;
}
