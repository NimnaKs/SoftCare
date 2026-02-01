package me.nimnakse.water_management.tariffs.dto.response;

public record TariffSlabRes(
        Long id,
        Integer gap,
        Integer fromUnit,
        Integer toUnit,
        Double charge,
        Double rental) {
}
