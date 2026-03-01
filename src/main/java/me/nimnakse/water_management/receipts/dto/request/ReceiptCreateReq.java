package me.nimnakse.water_management.receipts.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record ReceiptCreateReq(
        @NotNull Long cashAccountId,
        @NotBlank String paymentMethodCode,
        Long connectionId,
        String accountNumber,
        @NotNull @DecimalMin(value = "0.01") BigDecimal paidAmount,
        String chequeNo,
        String referenceNo,
        String remark,
        @NotBlank String receiptType,
        Long liabilityAccountId,
        String updateMobileNumber,
        @Valid @NotEmpty List<ReceiptSettlementReq> settlements
) {}
