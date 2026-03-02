package me.nimnakse.water_management.agencies.dto.response;

import me.nimnakse.water_management.agencies.entity.AgencyDepositRequestStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record AgencyTopupRequestRes(
        Long id,
        Long cashAccountId,
        String cashAccountName,
        Long paymentMethodId,
        String paymentMethodName,
        String referenceText,
        LocalDate paidDate,
        BigDecimal amount,
        AgencyDepositRequestStatus status,
        String attachmentName,
        Instant createdAt,
        String agencyName
) {
}

