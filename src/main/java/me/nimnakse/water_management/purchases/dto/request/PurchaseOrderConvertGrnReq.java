package me.nimnakse.water_management.purchases.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record PurchaseOrderConvertGrnReq(
        @NotBlank String grnNo,
        @NotNull LocalDate grnDate,
        @NotEmpty @Valid List<GrnInvoiceItemCreateReq> items
) {
}
