package me.nimnakse.water_management.address_lines.dto.response;

import java.time.Instant;

public record AddressLineRes(
        Long id,
        Integer level,
        String name,
        Long parentLine1Id,
        Long parentLine2Id,
        Long parentLine3Id,
        String postalCode,
        Instant createdAt,
        Instant updatedAt
) {
}
