package me.nimnakse.water_management.receipts.dto.response;

import java.time.Instant;

public record BulkReceiptUploadBatchRes(
        Long id,
        String fileName,
        String status,
        long totalRows,
        long postedRows,
        long failedRows,
        Instant createdAt
) {}
