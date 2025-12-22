package me.nimnakse.water_management.address_lines.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddressLineCreateReq(
        @NotNull Long orgUnitId,
        @NotNull Integer level,
        @NotBlank String name,
        Long parentLine1Id,
        Long parentLine2Id,
        Long parentLine3Id,
        String postalCode
) {
}
