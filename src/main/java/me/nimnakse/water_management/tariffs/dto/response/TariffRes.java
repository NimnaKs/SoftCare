package me.nimnakse.water_management.tariffs.dto.response;

import java.time.Instant;

public record TariffRes(
                Long id,
                String name,
                String description,
                Long orgUnitId,
                Instant createdAt,
                Instant updatedAt) {
}
