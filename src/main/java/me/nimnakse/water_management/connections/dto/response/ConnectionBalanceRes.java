package me.nimnakse.water_management.connections.dto.response;

import java.math.BigDecimal;
import java.util.List;
import me.nimnakse.water_management.receipts.dto.response.ReceiptSettlementPreviewItemRes;

public record ConnectionBalanceRes(
        Long connectionId,
        String accountNumber,
        BigDecimal debits,
        BigDecimal credits,
        BigDecimal upcoming,
        BigDecimal currentDue,
        BigDecimal total,
        List<ReceiptSettlementPreviewItemRes> openSettlements) {
}
