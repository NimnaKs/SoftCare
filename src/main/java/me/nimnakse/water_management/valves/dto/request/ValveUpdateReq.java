package me.nimnakse.water_management.valves.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ValveUpdateReq(
        @NotBlank String name
) {
}
