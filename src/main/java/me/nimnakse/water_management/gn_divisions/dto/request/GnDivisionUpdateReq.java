package me.nimnakse.water_management.gn_divisions.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GnDivisionUpdateReq(
        @NotBlank String name
) {
}
