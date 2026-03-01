package me.nimnakse.water_management.receipts.repository;

import me.nimnakse.water_management.receipts.entity.PaymentMethodLookup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodLookupRepository extends JpaRepository<PaymentMethodLookup, Long> {
    Optional<PaymentMethodLookup> findByCodeIgnoreCase(String code);
    List<PaymentMethodLookup> findByIsActiveTrue();
}
