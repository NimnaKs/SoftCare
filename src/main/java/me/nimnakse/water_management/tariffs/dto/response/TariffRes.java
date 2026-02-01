package me.nimnakse.water_management.tariffs.dto.response;

import java.time.Instant;
import java.util.List;

public record TariffRes(
        Long id,
        String name,
        String description,
        Long orgUnitId,
        Double newConnectionFee,
        Double reconnectionFee,
        Double reconnectionCreditLimit,
        Integer meterDigits,
        Double avgMonthlyMaxConsumption,
        String chargingMethod,
        List<TariffSlabRes> slabs,
        List<TariffLateFeeRes> lateFees,
        Instant createdAt,
        Instant updatedAt) {
}
