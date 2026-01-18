package me.nimnakse.water_management.purchases.dto.request;

import jakarta.validation.constraints.NotNull;

public record PurchaseOrderAssignSupplierReq(
        @NotNull Long supplierId
) {
}
