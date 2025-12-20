package me.nimnakse.water_management.connections.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ConnectionCreateReq(
        @NotNull Long memberId,
        @NotNull Long premisesId,
        @NotNull Long billingZoneId,
        @NotBlank String accountNumber,
        Long line1Id,
        Long line2Id,
        Long line3Id,
        Long line4Id,
        String houseNumber,
        String houseName,
        String houseNickname,
        Long gnDivisionId,
        Long valveId,
        Long societyId,
        Long clusterId,
        @NotBlank String mobileNumber,
        String secondaryNumber,
        String fixedLineNumber,
        @NotNull Long tariffId,
        @NotNull BigDecimal connectionFee,
        @NotNull Long invoiceId
) {
}
