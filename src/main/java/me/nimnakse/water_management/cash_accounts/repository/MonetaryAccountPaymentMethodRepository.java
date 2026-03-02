package me.nimnakse.water_management.cash_accounts.repository;

import me.nimnakse.water_management.cash_accounts.entity.MonetaryAccountPaymentMethod;
import me.nimnakse.water_management.cash_accounts.entity.MonetaryAccountPaymentMethodId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface MonetaryAccountPaymentMethodRepository extends JpaRepository<MonetaryAccountPaymentMethod, MonetaryAccountPaymentMethodId> {
    List<MonetaryAccountPaymentMethod> findByIdMonetaryAccountId(Long monetaryAccountId);

    List<MonetaryAccountPaymentMethod> findByIdMonetaryAccountIdIn(Collection<Long> monetaryAccountIds);

    void deleteByIdMonetaryAccountId(Long monetaryAccountId);
}

