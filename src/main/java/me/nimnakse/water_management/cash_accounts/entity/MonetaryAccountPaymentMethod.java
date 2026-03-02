package me.nimnakse.water_management.cash_accounts.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "monetary_account_payment_methods")
public class MonetaryAccountPaymentMethod {
    @EmbeddedId
    private MonetaryAccountPaymentMethodId id;
}

