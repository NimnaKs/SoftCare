package me.nimnakse.water_management.receipts.repository;

import me.nimnakse.water_management.receipts.entity.PaymentMethodLookup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodLookupRepository extends JpaRepository<PaymentMethodLookup, Long> {
    Optional<PaymentMethodLookup> findByCodeIgnoreCase(String code);
    List<PaymentMethodLookup> findByIsActiveTrue();

    @Query(value = """
            select pm.* from payment_methods pm
            inner join monetary_account_payment_methods map on map.payment_method_id = pm.id
            where map.monetary_account_id = :accountId
              and pm.is_active = 1
            order by pm.name asc
            """, nativeQuery = true)
    List<PaymentMethodLookup> findActiveByMonetaryAccountId(@Param("accountId") Long accountId);
}
