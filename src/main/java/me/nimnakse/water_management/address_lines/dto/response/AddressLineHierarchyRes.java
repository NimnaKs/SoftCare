package me.nimnakse.water_management.address_lines.dto.response;

public record AddressLineHierarchyRes(
        AddressLineRes line,
        AddressLineRes line1,
        AddressLineRes line2,
        AddressLineRes line3
) {
}
