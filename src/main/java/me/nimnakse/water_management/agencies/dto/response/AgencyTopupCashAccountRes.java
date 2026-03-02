package me.nimnakse.water_management.agencies.dto.response;

public record AgencyTopupCashAccountRes(
        Long id,
        String accountName,
        String type,
        String accountNumber
) {
}

