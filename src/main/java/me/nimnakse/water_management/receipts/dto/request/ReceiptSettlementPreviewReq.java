package me.nimnakse.water_management.receipts.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ReceiptSettlementPreviewReq(
        @NotNull Long connectionId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal paidAmount
) {}
