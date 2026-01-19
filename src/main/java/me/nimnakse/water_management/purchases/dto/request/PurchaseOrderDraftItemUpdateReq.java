package me.nimnakse.water_management.purchases.dto.request;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public record PurchaseOrderDraftItemUpdateReq(
        Long inventoryItemId,
        Long fixedAssetTemplateId,
        @DecimalMin(value = "0.00", message = "Unit cost must be zero or positive")
        BigDecimal unitCost
) {
}
