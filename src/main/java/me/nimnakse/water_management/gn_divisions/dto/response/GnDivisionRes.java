package me.nimnakse.water_management.gn_divisions.dto.response;

import java.time.Instant;

public record GnDivisionRes(
        Long id,
        String name,
        Long orgUnitId,
        Instant createdAt,
        Instant updatedAt) {
}
