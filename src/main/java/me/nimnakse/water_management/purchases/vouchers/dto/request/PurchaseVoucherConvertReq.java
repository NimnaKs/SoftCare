package me.nimnakse.water_management.purchases.vouchers.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PurchaseVoucherConvertReq(
                @NotNull LocalDate paymentDate,
                @NotNull Long fundSourceId) {
}
