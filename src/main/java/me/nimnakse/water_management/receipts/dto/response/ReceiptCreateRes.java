package me.nimnakse.water_management.receipts.dto.response;

public record ReceiptCreateRes(
        Long id,
        String receiptNo,
        String status
) {}
