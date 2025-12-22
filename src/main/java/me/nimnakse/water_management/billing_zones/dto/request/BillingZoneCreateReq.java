package me.nimnakse.water_management.billing_zones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BillingZoneCreateReq(
        @NotNull Long orgUnitId,
        @NotBlank String zoneName,
        String description,
        Integer sequenceNumber
) {
}
