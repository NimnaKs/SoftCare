package me.nimnakse.water_management.organization.dto.response;

public record OrganizationRes(
        Long id,
        Long orgUnitId,
        String organizationCode,
        String nameEn,
        String nameSi,
        String nameTa,
        String addressEn,
        String addressSi,
        String addressTa,
        String postalCode,
        String registrationNumber,
        String email,
        String mobileNumber,
        String telephoneNumber,
        String logoUrl
) {
}
