package me.nimnakse.water_management.cash_accounts.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "payment_methods")
public class PaymentMethod extends BaseEntity {
    @Column(nullable = false, length = 50, unique = true)
    private String code;

    @Column(nullable = false, length = 255, unique = true)
    private String name;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;
}
