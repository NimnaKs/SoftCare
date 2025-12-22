package me.nimnakse.water_management.premises.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PremisesUpdateReq(
        @NotNull Long billingZoneId,
        @NotBlank String premisesCode,
        @NotBlank String sortPath,
        Long parentId
) {
}
