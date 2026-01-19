package me.nimnakse.water_management.payments.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PaymentVoucherDraftUpdateReq(
        @NotEmpty @Valid List<PaymentVoucherDraftItemUpdateReq> items
) {
}
