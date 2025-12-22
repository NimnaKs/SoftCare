package me.nimnakse.water_management.societies.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SocietyUpdateReq(
        @NotBlank String name
) {
}
