package me.nimnakse.water_management.connections.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import me.nimnakse.water_management.connections.entity.ConnectionStatus;

public record ConnectionRes(
        Long id,
        Long memberId,
        Long premisesId,
        Long billingZoneId,
        String accountNumber,
        ConnectionStatus status,
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
        String mobileNumber,
        String secondaryNumber,
        String fixedLineNumber,
        Long tariffId,
        BigDecimal connectionFee,
        Long invoiceId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
