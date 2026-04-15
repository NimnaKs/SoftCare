package me.nimnakse.water_management.tariffs.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "tariffs")
public class Tariff extends BaseEntity {
    @Column(name = "org_unit_id", nullable = false)
    private Long orgUnitId;

    @Column(nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "new_connection_fee")
    private Double newConnectionFee;

    @Column(name = "reconnection_fee")
    private Double reconnectionFee;

    @Column(name = "reconnection_credit_limit")
    private Double reconnectionCreditLimit;

    @Column(name = "meter_digits")
    private Integer meterDigits;

    @Column(name = "avg_monthly_max_consumption")
    private Double avgMonthlyMaxConsumption;

    @Column(name = "zero_consumption_charge")
    private Double zeroConsumptionCharge;

    @Column(name = "charging_method")
    private String chargingMethod; // e.g., "Gap"

    @OneToMany(mappedBy = "tariff", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fromUnit ASC")
    private List<TariffSlab> slabs = new ArrayList<>();

    @OneToMany(mappedBy = "tariff", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TariffLateFee> lateFees = new ArrayList<>();
}
