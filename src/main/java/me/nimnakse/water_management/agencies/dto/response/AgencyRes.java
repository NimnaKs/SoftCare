package me.nimnakse.water_management.agencies.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import me.nimnakse.water_management.agencies.entity.BillingMode;

public record AgencyRes(
                Long id,
                Long organizationId,
                Long orgUnitId,
                String organizationCode,
                String businessName,
                String mobileNumber,
                String nicNumber,
                String businessAddress,
                String brcNumber,
                String ownerName,
                String secondaryContactNo,
                BigDecimal serviceChargePercent,
                BigDecimal subscriptionFee,
                BigDecimal totalCharges,
                BigDecimal creditLimit,
                BigDecimal walletAmount,
                BillingMode billingMode,
                Boolean isActive,
                Instant createdAt,
                Instant updatedAt) {
}
