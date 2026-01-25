package me.nimnakse.water_management.agencies.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import me.nimnakse.water_management.common.entity.BaseEntity;
import me.nimnakse.water_management.organization.entity.Organization;

@Getter
@Setter
@Entity
@Table(name = "agencies")
public class Agency extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "business_name", nullable = false, length = 255)
    private String businessName;

    @Column(name = "mobile_number", nullable = false, length = 20)
    private String mobileNumber;

    @Column(name = "nic_number", nullable = false, length = 30)
    private String nicNumber;

    @Column(name = "business_address", nullable = false, length = 500)
    private String businessAddress;

    @Column(name = "brc_number", length = 100)
    private String brcNumber;

    @Column(name = "owner_name", nullable = false, length = 255)
    private String ownerName;

    @Column(name = "secondary_contact_no", length = 20)
    private String secondaryContactNo;

    @Column(name = "service_charge_percent", nullable = false, precision = 6, scale = 2)
    private BigDecimal serviceChargePercent;

    @Column(name = "subscription_fee", nullable = false, precision = 14, scale = 2)
    private BigDecimal subscriptionFee;

    @Column(name = "total_charges", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalCharges;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_mode", nullable = false, length = 8)
    private BillingMode billingMode = BillingMode.PREPAID;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
