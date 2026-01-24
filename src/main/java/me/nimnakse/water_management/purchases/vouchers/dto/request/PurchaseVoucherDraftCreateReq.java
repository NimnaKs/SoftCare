package me.nimnakse.water_management.purchases.vouchers.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PurchaseVoucherDraftCreateReq(
        @NotNull Long orgUnitId,
        @NotEmpty List<Long> grnInvoiceIds) {
}
