package me.nimnakse.water_management.billing_zones.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BillingZoneReorderReq(
        @NotEmpty List<Long> zoneIdOrder) {
}
