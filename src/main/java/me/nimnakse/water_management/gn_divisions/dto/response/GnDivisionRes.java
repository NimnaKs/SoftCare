package me.nimnakse.water_management.gn_divisions.dto.response;

import java.time.Instant;

public record GnDivisionRes(
        Long id,
        String name,
        Instant createdAt,
        Instant updatedAt
) {
}
