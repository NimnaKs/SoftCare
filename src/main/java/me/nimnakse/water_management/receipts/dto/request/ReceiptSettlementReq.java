package me.nimnakse.water_management.receipts.dto.request;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record ReceiptSettlementReq(
        Long invoiceId,
        Long installmentId,
        @DecimalMin(value = "0.00") BigDecimal settledAmount
) {}
