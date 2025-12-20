package me.nimnakse.water_management.address_lines.dto.response;

import java.time.OffsetDateTime;

public record AddressLineRes(
        Long id,
        Integer level,
        String name,
        Long parentLine1Id,
        Long parentLine2Id,
        Long parentLine3Id,
        String postalCode,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
