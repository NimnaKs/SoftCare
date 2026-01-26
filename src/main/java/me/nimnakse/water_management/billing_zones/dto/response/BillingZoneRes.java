package me.nimnakse.water_management.billing_zones.dto.response;

import java.time.Instant;

public record BillingZoneRes(
                Long id,
                Long orgUnitId,
                Long clusterId,
                String zoneName,
                String description,
                String zoneCode,
                Integer sequenceNumber,
                Instant createdAt,
                Instant updatedAt) {
}
