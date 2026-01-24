package me.nimnakse.water_management.purchases.vouchers.dto.response;

import java.math.BigDecimal;

public record PurchaseVoucherDraftItemRes(
        Long id,
        Long grnInvoiceId,
        BigDecimal amount) {
}
