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
@Table(name = "tariff_slabs")
public class TariffSlab extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariff_id", nullable = false)
    private Tariff tariff;

    @Column(name = "gap")
    private Integer gap;

    @Column(name = "from_unit", nullable = false)
    private Integer fromUnit;

    @Column(name = "to_unit", nullable = false)
    private Integer toUnit;

    @Column(name = "charge")
    private Double charge;

    @Column(name = "rental")
    private Double rental;
}
