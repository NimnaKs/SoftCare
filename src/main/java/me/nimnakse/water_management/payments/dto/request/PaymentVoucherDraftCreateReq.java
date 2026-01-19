package me.nimnakse.water_management.payments.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PaymentVoucherDraftCreateReq(
        @NotNull Long orgUnitId,
        String referenceNo,
        @NotEmpty @Valid List<PaymentVoucherDraftItemCreateReq> items
) {
}
