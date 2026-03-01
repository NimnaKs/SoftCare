package me.nimnakse.water_management.receipts.dto.response;

public record PaymentMethodRes(
        Long id,
        String code,
        String name,
        Boolean isActive
) {}
