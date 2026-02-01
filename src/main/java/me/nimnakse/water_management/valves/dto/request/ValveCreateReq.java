package me.nimnakse.water_management.valves.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ValveCreateReq(
        @NotBlank String name,
        Long orgUnitId) {
}
