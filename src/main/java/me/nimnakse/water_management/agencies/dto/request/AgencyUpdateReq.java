package me.nimnakse.water_management.agencies.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import me.nimnakse.water_management.agencies.entity.BillingMode;

public record AgencyUpdateReq(
                @NotNull Long organizationId,
                @NotBlank String businessName,
                @NotBlank String mobileNumber,
                @NotBlank String nicNumber,
                @NotBlank String businessAddress,
                String brcNumber,
                @NotBlank String ownerName,
                String secondaryContactNo,
                @DecimalMin(value = "0.00") BigDecimal serviceChargePercent,
                @DecimalMin(value = "0.00") BigDecimal subscriptionFee,
                @DecimalMin(value = "0.00") BigDecimal totalCharges,
                BigDecimal creditLimit,
                BillingMode billingMode,
                Boolean isActive) {
}
