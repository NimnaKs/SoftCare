package me.nimnakse.water_management.cash_accounts.repository;

import java.util.List;
import me.nimnakse.water_management.cash_accounts.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    long countByIdIn(List<Long> ids);

    List<PaymentMethod> findByIdIn(List<Long> ids);
}
