package me.nimnakse.water_management.premises.dto.response;

public record PremisesValidationRes(
        Long premisesId,
        String premisesCode,
        String accountNumber,
        String memberName,
        String tariffName,
        String address) {
}
