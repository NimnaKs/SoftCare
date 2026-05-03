package me.nimnakse.water_management.receipts.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record ReceiptListRes(
        Long id,
        Long unrecognizedReceiptId,
        String receiptNo,
        String receiptCode,
        Long connectionId,
        String connectionAccountNumber,
        String memberName,
        Instant createdAt,
        BigDecimal amount,
        String description,
        Long cashAccountId,
        String cashAccountName,
        String createdByUsername,
        String paymentMethod,
        String statementStatus,
        String transactionStatus,
        String status,
        String receiptType
) {}
