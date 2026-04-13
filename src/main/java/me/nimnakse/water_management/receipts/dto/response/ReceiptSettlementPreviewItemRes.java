package me.nimnakse.water_management.receipts.dto.response;

import java.math.BigDecimal;

public record ReceiptSettlementPreviewItemRes(
        Long invoiceId,
        Long installmentId,
        String invoiceNo,
        BigDecimal dueAmount,
        BigDecimal settleAmount,
        boolean overdue
) {}