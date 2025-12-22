package me.nimnakse.water_management.clusters.dto.response;

import java.time.Instant;

public record ClusterRes(
        Long id,
        String name,
        Instant createdAt,
        Instant updatedAt
) {
}
