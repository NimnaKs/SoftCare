package me.nimnakse.water_management.tariffs.dto.request;

public record TariffLateFeeReq(
        String billType,
        String chargingMethod,
        String name,
        Integer overduePeriod,
        Double limitExceeded,
        Double fixedAmount,
        Double percentage) {
}
