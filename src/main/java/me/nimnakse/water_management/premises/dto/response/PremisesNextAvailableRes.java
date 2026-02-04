package me.nimnakse.water_management.premises.dto.response;

public record PremisesNextAvailableRes(
        Long billingZoneId,
        Long parentId,
        String premisesCode,
        String sortPath
) {
}
