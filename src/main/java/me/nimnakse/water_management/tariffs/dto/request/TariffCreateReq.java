package me.nimnakse.water_management.tariffs.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TariffCreateReq(
                @NotBlank String name,
                String description,
                Long orgUnitId) {
}
