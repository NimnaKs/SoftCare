package me.nimnakse.water_management.receipts.dto.response;

import java.time.Instant;

public record ChequeTrackingRes(
        String chequeNo,
        Long receiptId,
        String receiptNo,
        String status,
        String note,
        Instant updatedAt
) {}
