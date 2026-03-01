package me.nimnakse.water_management.receipts.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ReceiptSettlementPreviewRes(
        BigDecimal currentDue,
        BigDecimal paidAmount,
        BigDecimal remainingBalance,
        List<ReceiptSettlementPreviewItemRes> settlements,
        boolean overpaymentWarning
) {}
