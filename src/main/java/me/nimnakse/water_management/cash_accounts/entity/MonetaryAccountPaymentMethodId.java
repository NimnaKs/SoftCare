package me.nimnakse.water_management.cash_accounts.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MonetaryAccountPaymentMethodId implements Serializable {
    @Column(name = "monetary_account_id")
    private Long monetaryAccountId;

    @Column(name = "payment_method_id")
    private Long paymentMethodId;

    public MonetaryAccountPaymentMethodId() {
    }

    public MonetaryAccountPaymentMethodId(Long monetaryAccountId, Long paymentMethodId) {
        this.monetaryAccountId = monetaryAccountId;
        this.paymentMethodId = paymentMethodId;
    }

    public Long getMonetaryAccountId() {
        return monetaryAccountId;
    }

    public void setMonetaryAccountId(Long monetaryAccountId) {
        this.monetaryAccountId = monetaryAccountId;
    }

    public Long getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(Long paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MonetaryAccountPaymentMethodId that = (MonetaryAccountPaymentMethodId) o;
        return Objects.equals(monetaryAccountId, that.monetaryAccountId)
                && Objects.equals(paymentMethodId, that.paymentMethodId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(monetaryAccountId, paymentMethodId);
    }
}
