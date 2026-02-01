package me.nimnakse.water_management.tariffs.dto.response;

public record TariffLateFeeRes(
        Long id,
        String billType,
        String chargingMethod,
        String name,
        Integer overduePeriod,
        Double limitExceeded,
        Double fixedAmount,
        Double percentage) {
}
