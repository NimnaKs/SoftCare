package me.nimnakse.water_management.cash_accounts.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "monetary_account_payment_methods")
public class MonetaryAccountPaymentMethod {
    @EmbeddedId
    private MonetaryAccountPaymentMethodId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("monetaryAccountId")
    @JoinColumn(name = "monetary_account_id")
    private MonetaryAccount monetaryAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("paymentMethodId")
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    public MonetaryAccountPaymentMethod(MonetaryAccount monetaryAccount, PaymentMethod paymentMethod) {
        this.monetaryAccount = monetaryAccount;
        this.paymentMethod = paymentMethod;
        this.id = new MonetaryAccountPaymentMethodId(monetaryAccount.getId(), paymentMethod.getId());
    }
}
