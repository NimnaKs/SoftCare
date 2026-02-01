package me.nimnakse.water_management.tariffs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "tariff_late_fees")
public class TariffLateFee extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariff_id", nullable = false)
    private Tariff tariff;

    @Column(name = "apply_normal_bill")
    private boolean applyNormalBill;

    @Column(name = "apply_red_bill")
    private boolean applyRedBill;

    @Column(name = "charging_method")
    private String chargingMethod; // Fixed Amount, Percentage

    @Column(name = "name")
    private String name; // 1 Circle, 2 Circle, etc.

    @Column(name = "overdue_period")
    private Integer overduePeriod;

    @Column(name = "limit_exceeded")
    private Double limitExceeded;

    @Column(name = "fixed_amount")
    private Double fixedAmount;

    @Column(name = "percentage")
    private Double percentage;
}
