package me.nimnakse.water_management.valves.dto.response;

import java.time.Instant;

public record ValveRes(
        Long id,
        String name,
        Instant createdAt,
        Instant updatedAt
) {
}
