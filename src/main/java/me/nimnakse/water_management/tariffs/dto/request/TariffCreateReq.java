package me.nimnakse.water_management.tariffs.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record TariffCreateReq(
        @NotBlank String name,
        String description,
        Long orgUnitId,
        Double newConnectionFee,
        Double reconnectionFee,
        Double reconnectionCreditLimit,
        Integer meterDigits,
        Double avgMonthlyMaxConsumption,
        String chargingMethod,
        List<TariffSlabReq> slabs,
        List<TariffLateFeeReq> lateFees) {
}
