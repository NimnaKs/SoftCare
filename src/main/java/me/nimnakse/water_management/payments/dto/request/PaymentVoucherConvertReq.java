package me.nimnakse.water_management.payments.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PaymentVoucherConvertReq(
        @NotNull LocalDate paymentDate,
        @NotNull Long fundSourceId,
        @NotNull Long paymentMethodId) {
}
