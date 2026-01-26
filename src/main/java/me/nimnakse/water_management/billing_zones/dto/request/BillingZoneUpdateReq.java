package me.nimnakse.water_management.billing_zones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BillingZoneUpdateReq(
                @NotNull Long orgUnitId,
                Long clusterId,
                @NotBlank String zoneName,
                String description,
                @NotNull Integer sequenceNumber) {
}
