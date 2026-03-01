package me.nimnakse.water_management.receipts.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ConnectionByAccountRes(
        Long connectionId,
        String accountNumber,
        String memberName,
        String mobileNumber,
        String address,
        BigDecimal currentDue,
        List<ReceiptSettlementPreviewItemRes> openSettlements
) {}
