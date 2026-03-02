package me.nimnakse.water_management.subscription_payments.dto.response;

import me.nimnakse.water_management.subscription_payments.entity.SubscriptionPaymentRequestStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record SubscriptionPaymentRequestRes(
        Long id,
        Long agencyId,
        String cashAccountLabel,
        Long paymentMethodId,
        String paymentMethodName,
        String referenceText,
        LocalDate paidDate,
        BigDecimal amount,
        SubscriptionPaymentRequestStatus status,
        String attachmentName,
        Instant createdAt
) {}