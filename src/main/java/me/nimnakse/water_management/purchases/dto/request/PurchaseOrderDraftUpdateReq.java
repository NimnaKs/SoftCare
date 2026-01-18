package me.nimnakse.water_management.purchases.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PurchaseOrderDraftUpdateReq(
        @NotEmpty @Valid List<PurchaseOrderDraftItemUpdateReq> items
) {
}
