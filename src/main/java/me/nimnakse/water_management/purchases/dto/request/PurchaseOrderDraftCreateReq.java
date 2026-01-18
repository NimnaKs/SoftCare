package me.nimnakse.water_management.purchases.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PurchaseOrderDraftCreateReq(
        @NotNull Long orgUnitId,
        String referenceNo,
        @NotEmpty @Valid List<PurchaseOrderDraftItemCreateReq> items
) {
}
