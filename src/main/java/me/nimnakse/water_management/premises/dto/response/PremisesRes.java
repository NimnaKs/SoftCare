package me.nimnakse.water_management.premises.dto.response;

import java.time.Instant;

public record PremisesRes(
        Long id,
        Long billingZoneId,
        String premisesCode,
        String sortPath,
        Long parentId,
        Instant createdAt,
        Instant updatedAt
) {
}
