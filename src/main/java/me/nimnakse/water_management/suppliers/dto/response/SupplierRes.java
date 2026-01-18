package me.nimnakse.water_management.suppliers.dto.response;

import java.time.Instant;

public record SupplierRes(
        Long id,
        Long orgUnitId,
        String supplierCode,
        String name,
        String address,
        String brcNumber,
        String nic,
        String mobileNumber1,
        String mobileNumber2,
        String telephoneNumber,
        String email,
        Long liabilityAccountId,
        Boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
}
