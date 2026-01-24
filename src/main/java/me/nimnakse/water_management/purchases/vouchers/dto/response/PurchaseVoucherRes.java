package me.nimnakse.water_management.purchases.vouchers.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PurchaseVoucherRes(
                Long id,
                Long orgUnitId,
                String voucherNo,
                Long draftId,
                BigDecimal totalAmount,
                LocalDate paymentDate,
                Long fundSourceId,
                List<PurchaseVoucherItemRes> items,
                Instant createdAt,
                Instant updatedAt) {
}
