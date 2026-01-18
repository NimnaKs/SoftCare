package me.nimnakse.water_management.cash_accounts.repository;

import java.util.List;
import me.nimnakse.water_management.cash_accounts.entity.MonetaryAccountPaymentMethod;
import me.nimnakse.water_management.cash_accounts.entity.MonetaryAccountPaymentMethodId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonetaryAccountPaymentMethodRepository
        extends JpaRepository<MonetaryAccountPaymentMethod, MonetaryAccountPaymentMethodId> {
    List<MonetaryAccountPaymentMethod> findByIdMonetaryAccountId(Long monetaryAccountId);

    void deleteByIdMonetaryAccountId(Long monetaryAccountId);
}
