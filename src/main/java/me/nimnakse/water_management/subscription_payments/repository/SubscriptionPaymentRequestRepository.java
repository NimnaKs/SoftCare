package me.nimnakse.water_management.subscription_payments.repository;

import me.nimnakse.water_management.subscription_payments.entity.SubscriptionPaymentRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionPaymentRequestRepository extends JpaRepository<SubscriptionPaymentRequest, Long> {
    List<SubscriptionPaymentRequest> findByAgencyIdOrderByCreatedAtDesc(Long agencyId);
}
