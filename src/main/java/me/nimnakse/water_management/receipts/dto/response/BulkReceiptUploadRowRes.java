package me.nimnakse.water_management.receipts.dto.response;

public record BulkReceiptUploadRowRes(
        Long id,
        long rowNo,
        String accountNumber,
        java.math.BigDecimal paidAmount,
        String status,
        String errorMessage,
        Long receiptId,
        String receiptNo
) {}
