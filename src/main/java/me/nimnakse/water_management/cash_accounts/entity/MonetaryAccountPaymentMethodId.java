package me.nimnakse.water_management.cash_accounts.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class MonetaryAccountPaymentMethodId implements Serializable {
    @Column(name = "monetary_account_id")
    private Long monetaryAccountId;

    @Column(name = "payment_method_id")
    private Long paymentMethodId;
}

