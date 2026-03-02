package me.nimnakse.water_management.agencies.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import me.nimnakse.water_management.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "agency_deposit_requests")
public class AgencyDepositRequest extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;

    @Column(name = "agency_id", insertable = false, updatable = false)
    private Long agencyId;

    @Column(name = "monetary_account_id", nullable = false)
    private Long monetaryAccountId;

    @Column(name = "payment_method_id", nullable = false)
    private Long paymentMethodId;

    @Column(name = "reference_text", nullable = false, length = 255)
    private String referenceText;

    @Column(name = "paid_date", nullable = false)
    private LocalDate paidDate;

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AgencyDepositRequestStatus status = AgencyDepositRequestStatus.REQUESTED;

    @Column(name = "attachment_name", length = 255)
    private String attachmentName;

    @Column(name = "attachment_path", length = 500)
    private String attachmentPath;
}
