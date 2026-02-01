package me.nimnakse.water_management.societies.dto.response;

import java.time.Instant;

public record SocietyRes(
                Long id,
                String name,
                Long orgUnitId,
                Instant createdAt,
                Instant updatedAt) {
}
