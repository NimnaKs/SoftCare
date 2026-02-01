package me.nimnakse.water_management.tariffs.dto.request;

public record TariffSlabReq(
        Integer gap,
        Integer fromUnit,
        Integer toUnit,
        Double charge,
        Double rental) {
}
